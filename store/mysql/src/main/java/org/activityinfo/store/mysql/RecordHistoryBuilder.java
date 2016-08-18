package org.activityinfo.store.mysql;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.activityinfo.api.client.FormHistoryEntryBuilder;
import org.activityinfo.api.client.FormValueChangeBuilder;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.barcode.BarcodeValue;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.model.type.time.LocalDateType;
import org.activityinfo.service.store.FormAccessor;
import org.activityinfo.service.store.FormNotFoundException;
import org.activityinfo.service.store.RecordVersion;
import org.activityinfo.store.query.impl.Slot;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Assembles a history of 
 */
public class RecordHistoryBuilder {
    
    private MySqlCatalog catalog;

    private static class FieldDelta {
        private FormField field;
        private FieldValue oldValue;
        private FieldValue newValue;
    }
    
    
    private static class RecordDelta {
        private RecordVersion version;
        private final List<FieldDelta> changes = new ArrayList<>();
    }
    
    private static class User {
        private String name;
        private String email;
    }
    
    public RecordHistoryBuilder(MySqlCatalog catalog) {
        this.catalog = catalog;
    }
    
    
    public JsonArray build(ResourceId formId, ResourceId recordId) throws SQLException {
        Optional<FormAccessor> form = catalog.getForm(formId);
        if(!form.isPresent()) {
            throw new FormNotFoundException(formId);    
        }
        
        // Build a map of fields that we need
        Map<ResourceId, FormField> fieldMap = new HashMap<>();
        FormClass formClass = form.get().getFormClass();
        for (FormField field : formClass.getFields()) {
            fieldMap.put(field.getId(), field);
        }
        
        
        List<RecordVersion> versions = form.get().getVersions(recordId);
        if(versions.isEmpty()) {
            throw new IllegalStateException("No versions for record " + recordId);
        }
        
        // Query users involved in the changes
        Map<Long, User> userMap = queryUsers(versions);
        
        
        // Build a list of deltas
        List<RecordDelta> deltas = computeDeltas(formClass, versions);
        
        // Now render the complete object for the user
        JsonArray array = new JsonArray();
        for (RecordDelta delta : deltas) {
            
            User user = userMap.get(delta.version.getUserId());
            if(user == null) {
                user = new User();
                user.email = delta.version.getUserId() + "@activityinfo.org";
                user.name = "User " + delta.version.getUserId();
            }
            
            FormHistoryEntryBuilder entry = new FormHistoryEntryBuilder();
            entry.setFormId(formId.asString());
            entry.setRecordId(recordId.asString());
            entry.setTime((int)(delta.version.getTime() / 1000));
            entry.setChangeType(delta.version.getType().name().toLowerCase());
            entry.setUserName(user.name);
            entry.setUserEmail(user.email);

            for (FieldDelta change : delta.changes) {
                entry.addValue(renderChange(change));
            }
            array.add(entry.toJsonObject());
        }
        return array;
    }

    
    private List<RecordDelta> computeDeltas(FormClass formClass, List<RecordVersion> versions) {
        List<RecordDelta> deltas = new ArrayList<>();
        Map<ResourceId, FieldValue> currentState = new HashMap<>();
        Iterator<RecordVersion> versionIt = versions.iterator();

        RecordDelta initial = new RecordDelta();
        initial.version = versionIt.next();
        deltas.add(initial);
        
        currentState.putAll(initial.version.getValues());

        while(versionIt.hasNext()) {
            RecordVersion version = versionIt.next();
            RecordDelta delta = new RecordDelta();
            delta.version = version;
            
            for (FormField field : formClass.getFields()) {
                FieldValue oldValue = currentState.get(field.getId());
                if(version.getValues().containsKey(field.getId())) {
                    FieldValue newValue = version.getValues().get(field.getId());
                    if (!Objects.equals(oldValue, newValue)) {
                        FieldDelta fieldDelta = new FieldDelta();
                        fieldDelta.field = field;
                        fieldDelta.oldValue = oldValue;
                        fieldDelta.newValue = newValue;
                        delta.changes.add(fieldDelta);
                    }
                    currentState.put(field.getId(),  newValue);
                }
            }
            deltas.add(delta);
        }
        return deltas;
    }

    private Map<Long, User> queryUsers(List<RecordVersion> versions) throws SQLException {
        Set<Long> userSet = new HashSet<>();
        for (RecordVersion version : versions) {
            userSet.add(version.getUserId());
        }
        
        Map<Long, User> userMap = new HashMap<>();
        
        if(userSet.isEmpty()) {
            return userMap;
        }
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT userId, name, email FROM userlogin WHERE userId IN (");
        boolean needsComma = false;
        for (Long userId : userSet) {
            if(needsComma) {
                sql.append(", ");
            }
            sql.append(userId);
            needsComma = true;
        }
        sql.append(")");
        
        try(ResultSet rs = catalog.getExecutor().query(sql.toString())) {
            while(rs.next()) {
                
                int userId = rs.getInt(1);
                
                User user = new User();
                user.name = rs.getString(2);
                user.email = rs.getString(3);
             
                userMap.put((long) userId, user);
            }
        }
        return userMap;
    }
    
    private FormValueChangeBuilder renderChange(FieldDelta delta) {
        FormValueChangeBuilder builder = new FormValueChangeBuilder();
        builder.setFieldId(delta.field.getId().asString());
        builder.setFieldLabel(delta.field.getLabel());
        builder.setOldValueLabel(renderValue(delta.field, delta.oldValue));
        builder.setNewValueLabel(renderValue(delta.field, delta.newValue));
        return builder;
    }
    
    private String renderValue(FormField field, FieldValue value) {
        if(value == null) {
            return "";
        }
        if(field.getType() instanceof TextType) {
            return ((TextValue) value).toString();
        }
        if(field.getType() instanceof NarrativeType) {
            return ((NarrativeValue)value).toString();
        } 
        if(field.getType() instanceof BarcodeType) {
            return ((BarcodeValue)value).toString();
        }
        if(field.getType() instanceof EnumType) {
            return render((EnumType)field.getType(), (EnumValue)value);
        }
        if(field.getType() instanceof QuantityType) {
            Quantity quantity = (Quantity) value;
            return Double.toString(quantity.getValue());
        } 
        if(field.getType() instanceof LocalDateType) {
            return ((LocalDate) value).toString();
        } 
        if(field.getType() instanceof ReferenceType) {
            return renderRef((ReferenceType)field.getType(), (ReferenceValue)value);
        }
        return "";
    }

    private String renderRef(ReferenceType type, ReferenceValue value) {
       return Joiner.on(", ").join(queryLabels(type, value));
    }

    private List<String> queryLabels(ReferenceType type, ReferenceValue value) {
        Map<ResourceId, String> labelMap = new HashMap<>();
        for (ResourceId formId : type.getRange()) {
            Optional<FormAccessor> form = catalog.getForm(formId);
            if(form.isPresent()) {
                ResourceId labelFieldId = findLabelField(form.get().getFormClass());
                if(labelFieldId == null) {
                    for (ResourceId recordId : value.getResourceIds()) {
                        Optional<FormRecord> record = form.get().get(recordId);
                        if (record.isPresent()) {
                            JsonElement labelValue = record.get().getFields().get(labelFieldId.asString());
                            if(labelValue.isJsonPrimitive()) {
                                labelMap.put(recordId, labelValue.getAsString());
                            }
                        }
                    }
                }
            }
        }

        List<String> list = new ArrayList<>();
        for (ResourceId recordId : value.getResourceIds()) {
            String label = labelMap.get(recordId);
            if(label == null) {
                list.add(recordId.asString());
            } else {
                list.add(label);
            }
        }
        return list;
    }
    
    private ResourceId findLabelField(FormClass formClass) {
        for (FormField field : formClass.getFields()) {
            if(field.getSuperProperties().contains(ResourceId.valueOf("label"))) {
                return field.getId();
            }
        }
        return null;
    }

    private String render(EnumType type, EnumValue value) {
        StringBuilder sb = new StringBuilder();
        boolean needsComma = false;
        for (EnumItem enumItem : value.getValuesAsItems(type)) {
            if(needsComma) {
                sb.append(", ");
            }
            sb.append(enumItem.getLabel());
            needsComma = true;
        }
        return sb.toString();
    }
}
