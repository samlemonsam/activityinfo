package org.activityinfo.store.mysql.collections;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.NarrativeValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.service.store.RecordChangeType;
import org.activityinfo.service.store.RecordVersion;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.metadata.Activity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads entries from SiteHistory
 */
public class SiteHistoryReader {

    private QueryExecutor executor;
    private Activity activity;
    private int siteId;
    
    private Map<ResourceId, FormField> fieldMap = new HashMap<>();

    private JsonParser parser = new JsonParser();

    public SiteHistoryReader(QueryExecutor executor, Activity activity, FormClass formClass, int siteId) {
        this.executor = executor;
        this.activity = activity;
        this.siteId = siteId;

        for (FormField formField : formClass.getFields()) {
            fieldMap.put(formField.getId(), formField);
        }
    }
    
    public List<RecordVersion> read() throws SQLException {
        String sql = "SELECT h.siteId, h.timecreated, h.initial, h.json, h.userId " +
                "FROM sitehistory h " +
                "WHERE siteid = " + siteId + " " + 
                "ORDER BY h.timecreated";
        
        List<RecordVersion> changes = new ArrayList<>();
        
        try(ResultSet rs = executor.query(sql)) {
            while(rs.next()) {
                int siteId = rs.getInt(1);
                long time = rs.getLong(2);
                boolean initial = rs.getBoolean(3);
                String json = rs.getString(4);
                long userId = rs.getInt(5);
                
                RecordVersion change = new RecordVersion();
                change.setRecordId(CuidAdapter.cuid(CuidAdapter.SITE_DOMAIN, siteId));
                change.setTime(time);
                change.setUserId(userId);

                JsonObject jsonObject = (JsonObject) parser.parse(json);
                
                if(jsonObject.has("_DELETE")) {
                    change.setType(RecordChangeType.DELETED);
                } else {
                    change.setType(initial ? RecordChangeType.CREATED : RecordChangeType.UPDATED);
                    change.getValues().putAll(parseChanges(jsonObject));
                }

                changes.add(change);
            }
        }
        
        return changes;
        
    }

    private Map<ResourceId, FieldValue> parseChanges(JsonObject jsonObject) {

        Map<ResourceId, FieldValue> valueMap = new HashMap<>();
        
        for (Map.Entry<String, JsonElement> jsonEntry : jsonObject.entrySet()) {
            String fieldName = jsonEntry.getKey();
            if(fieldName.equals("comments")) {
                valueMap.put(fieldId(CuidAdapter.COMMENT_FIELD), 
                        NarrativeValue.valueOf(parseString(jsonEntry.getValue())));

            } else if(fieldName.equals("date1")) {
                valueMap.put(fieldId(CuidAdapter.START_DATE_FIELD), parseDate(jsonEntry.getValue()));

            } else if(fieldName.equals("date2")) {
                valueMap.put(fieldId(CuidAdapter.START_DATE_FIELD), parseDate(jsonEntry.getValue()));

            } else if(fieldName.equals("partnerId")) {
                valueMap.put(fieldId(CuidAdapter.PARTNER_FIELD), 
                        parseRef(jsonEntry.getValue(), activity.getPartnerFormClassId(), CuidAdapter.PARTNER_DOMAIN));

            } else if(fieldName.equals("projectId")) {
                valueMap.put(fieldId(CuidAdapter.PROJECT_FIELD),
                        parseRef(jsonEntry.getValue(), activity.getProjectFormClassId(), CuidAdapter.PROJECT_DOMAIN));

            } else if(fieldName.equals("locationId")) {
                valueMap.put(fieldId(CuidAdapter.LOCATION_FIELD),
                        parseRef(jsonEntry.getValue(), activity.getLocationFormClassId(), CuidAdapter.LOCATION_DOMAIN));

            } else if(fieldName.startsWith("I")) {
                int mIndex = fieldName.indexOf("M");
                if(mIndex == -1) {
                    int indicatorId = Integer.parseInt(fieldName.substring(1));
                    ResourceId fieldId = CuidAdapter.indicatorField(indicatorId);
                    valueMap.put(fieldId, parseQuantity(jsonEntry.getValue()));
                } else { // old history
                    valueMap.put(ResourceId.valueOf(fieldName), parseQuantity(jsonEntry.getValue()));
                }
            }
        }
        return valueMap;
    }

    private FieldValue parseRef(JsonElement value, ResourceId formId, char domain) {
        if(value.isJsonObject()) {
            JsonObject object = value.getAsJsonObject();
            if(object.get("type").getAsString().equals("Integer")) {
                int id = object.get("value").getAsInt();
                ResourceId recordId = CuidAdapter.cuid(domain, id);
                return new ReferenceValue(new RecordRef(formId, recordId));
            }
        }
        return null;
    }

    private FieldValue parseDate(JsonElement value) {
        if(value.isJsonObject()) {
            JsonObject object = value.getAsJsonObject();
            if(object.get("type").getAsString().equals("LocalDate")) {
                return LocalDate.parse(object.get("value").getAsString());
            }
        }
        return null;
    }

    private FieldValue parseQuantity(JsonElement value) {
        if(value.isJsonObject()) {
            JsonObject object = value.getAsJsonObject();
            if(object.get("type").getAsString().equals("Double")) {
                return new Quantity(object.get("value").getAsDouble());
            }
        }
        return null;
    }

    private String parseString(JsonElement value) {
        if(value.isJsonObject()) {
            JsonObject object = value.getAsJsonObject();
            if(object.get("type").getAsString().equals("String")) {
                return Strings.emptyToNull(object.get("value").getAsString().trim());
            }
        }
        return null;
    }

    private ResourceId fieldId(int fieldIndex) {
        return CuidAdapter.field(activity.getSiteFormClassId(), fieldIndex);
    }

}
