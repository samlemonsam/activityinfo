package org.activityinfo.store.mysql.collections;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.activityinfo.api.client.FormHistoryEntryBuilder;
import org.activityinfo.api.client.FormValueChangeBuilder;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.metadata.Activity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Reads entries from SiteHistory
 */
public class SiteHistoryReader {

    private QueryExecutor executor;
    private Activity activity;
    private FormClass formClass;
    private int siteId;
    
    private Map<ResourceId, FormField> fieldMap = new HashMap<>();
    
    private boolean firstEntry;
    private Map<ResourceId, FieldValue> stateMap = new HashMap<>();
    
    public SiteHistoryReader(QueryExecutor executor, Activity activity, FormClass formClass, int siteId) {
        this.executor = executor;
        this.activity = activity;
        this.formClass = formClass;
        this.siteId = siteId;

        for (FormField formField : formClass.getFields()) {
            fieldMap.put(formField.getId(), formField);
        }
    }
    
    public List<FormHistoryEntryBuilder> read() throws SQLException {
        String sql = "SELECT h.siteId, h.timecreated, h.initial, h.json, u.name, u.email " +
                "FROM sitehistory h " +
                "LEFT JOIN userlogin u ON (u.userId=h.userId) " + 
                "WHERE siteid = " + siteId + " " + 
                "ORDER BY h.timecreated";
        
        List<FormHistoryEntryBuilder> entryList = new ArrayList<>();
        
        firstEntry = true;
        
        try(ResultSet rs = executor.query(sql)) {
            while(rs.next()) {
                int siteId = rs.getInt(1);
                long time = rs.getLong(2);
                boolean initial = rs.getBoolean(3);
                String json = rs.getString(4);
                String userName = rs.getString(5);
                String userEmail = rs.getString(6);
                
                FormHistoryEntryBuilder entry = new FormHistoryEntryBuilder();
                entry.setFormId(activity.getSiteFormClassId().asString());
                entry.setRecordId(CuidAdapter.cuid(CuidAdapter.SITE_DOMAIN, siteId).asString());
                entry.setTime((int) (time / 1000));
                entry.setUserName(userName);
                entry.setUserEmail(userEmail);
                entry.setChangeType(initial ? "created" : "updated");
                
                parseChanges(json, entry);
                
                firstEntry = false;
                
                entryList.add(entry);
            }
        }
        
        return entryList;
        
    }

    private void parseChanges(String json, FormHistoryEntryBuilder historyEntry) {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = (JsonObject) parser.parse(json);


        for (Map.Entry<String, JsonElement> jsonEntry : jsonObject.entrySet()) {
            String fieldName = jsonEntry.getKey();
            if(fieldName.equals("comments")) {
                addValueIfChanged(historyEntry, fieldId(CuidAdapter.COMMENT_FIELD), parseString(jsonEntry.getValue()));
            
            } else if(fieldName.startsWith("I")) {
                int indicatorId = Integer.parseInt(fieldName.substring(1));
                ResourceId fieldId = CuidAdapter.indicatorField(indicatorId);
                
                addValueIfChanged(historyEntry, fieldId, parseQuantity(jsonEntry.getValue()));
            }
        }
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

    private FieldValue parseString(JsonElement value) {
        if(value.isJsonObject()) {
            JsonObject object = value.getAsJsonObject();
            if(object.get("type").getAsString().equals("String")) {
                return TextValue.valueOf(Strings.emptyToNull(object.get("value").getAsString().trim()));
            }
        }
        return null;
    }
    
    private void addValueIfChanged(FormHistoryEntryBuilder historyEntry, ResourceId fieldId, FieldValue newValue) {
        
        FormField field = fieldMap.get(fieldId);
        
        // If a field has been deleted, just skip for now
        if(field == null) {
            return;
        }
        
        if(!firstEntry) {
            FieldValue oldValue = stateMap.get(fieldId);
            if(!Objects.equals(oldValue, newValue)) {
                historyEntry.addValue(new FormValueChangeBuilder()
                    .setFieldId(fieldId.asString())
                    .setFieldLabel(field.getLabel())
                    .setNewValueLabel(valueLabel(newValue))
                    .setOldValueLabel(valueLabel(oldValue)));
            }
        }

        stateMap.put(fieldId, newValue);
    }

    private String valueLabel(FieldValue value) {
        if(value == null) {
            return "";
        } else if(value instanceof TextValue) {
            return ((TextValue) value).asString();
        } else if(value instanceof Quantity) {
            return Double.toString( ((Quantity) value).getValue() );
        } else {
            return "(" +  value.getTypeClass().getId() + ")";
        }
    }

    private ResourceId fieldId(int fieldIndex) {
        return CuidAdapter.field(activity.getSiteFormClassId(), fieldIndex);
    }

}
