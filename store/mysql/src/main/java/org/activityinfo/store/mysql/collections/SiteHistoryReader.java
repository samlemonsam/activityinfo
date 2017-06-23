package org.activityinfo.store.mysql.collections;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.activityinfo.json.JsonObject;
import org.activityinfo.json.JsonParser;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.NarrativeValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.metadata.Activity;
import org.activityinfo.store.mysql.metadata.ActivityField;
import org.activityinfo.store.spi.RecordChangeType;
import org.activityinfo.store.spi.RecordVersion;

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
                
                if(jsonObject.hasKey("_DELETE")) {
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

        Map<ResourceId, ResourceId> attributeToFieldMap = new HashMap<>();
        for (ActivityField activityField : activity.getAttributeAndIndicatorFields()) {
            if(activityField.getFormField().getType() instanceof EnumType) {
                EnumType type = (EnumType) activityField.getFormField().getType();
                for (EnumItem enumItem : type.getValues()) {
                    attributeToFieldMap.put(enumItem.getId(), activityField.getResourceId());
                }
            }
        }

        Map<ResourceId, FieldValue> valueMap = new HashMap<>();

        Multimap<ResourceId, ResourceId> attributeValueMap = HashMultimap.create();

        for (Map.Entry<String, JsonValue> jsonEntry : jsonObject.entrySet()) {
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
            } else if(fieldName.startsWith("ATTRIB")) {
                if(parseBoolean(jsonEntry.getValue())) {
                    int attributeId = Integer.parseInt(fieldName.substring("ATTRIB".length()));
                    ResourceId attributeCuid = CuidAdapter.attributeId(attributeId);
                    ResourceId fieldId = attributeToFieldMap.get(attributeCuid);
                    if (fieldId != null) {
                        attributeValueMap.put(fieldId, attributeCuid);
                    }
                }
            }
        }

        for (ResourceId fieldId : attributeValueMap.keySet()) {
            valueMap.put(fieldId, new EnumValue(attributeValueMap.get(fieldId)));
        }

        return valueMap;
    }

    private boolean parseBoolean(JsonValue value) {
        if(value.isJsonObject()) {
            JsonObject object = value.getAsJsonObject();
            if(object.hasKey("value")) {
                return object.get("value").asBoolean();
            }
        }
        return false;
    }

    private FieldValue parseRef(JsonValue value, ResourceId formId, char domain) {
        if(value.isJsonObject()) {
            JsonObject object = value.getAsJsonObject();
            if(object.get("type").asString().equals("Integer")) {
                int id = object.get("value").asInt();
                ResourceId recordId = CuidAdapter.cuid(domain, id);
                return new ReferenceValue(new RecordRef(formId, recordId));
            }
        }
        return null;
    }

    private FieldValue parseDate(JsonValue value) {
        if(value.isJsonObject()) {
            JsonObject object = value.getAsJsonObject();
            if(object.get("type").asString().equals("LocalDate")) {
                return LocalDate.parse(object.get("value").asString());
            }
        }
        return null;
    }

    private FieldValue parseQuantity(JsonValue value) {
        if(value.isJsonObject()) {
            JsonObject object = value.getAsJsonObject();
            if(object.get("type").asString().equals("Double")) {
                return new Quantity(object.get("value").asNumber());
            }
        }
        return null;
    }

    private String parseString(JsonValue value) {
        if(value.isJsonObject()) {
            JsonObject object = value.getAsJsonObject();
            if(object.get("type").asString().equals("String")) {
                return Strings.emptyToNull(object.get("value").asString().trim());
            }
        }
        return null;
    }

    private ResourceId fieldId(int fieldIndex) {
        return CuidAdapter.field(activity.getSiteFormClassId(), fieldIndex);
    }

}
