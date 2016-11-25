package org.activityinfo.store.mysql.update;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.metadata.Activity;
import org.activityinfo.store.mysql.metadata.ActivityField;

import java.util.*;


public class ActivityUpdater {

    private Activity activity;
    private QueryExecutor executor;
    private final long newVersion;
    private Map<ResourceId, ActivityField> fieldMap = Maps.newHashMap();

    public ActivityUpdater(Activity activity, QueryExecutor executor) {
        this.activity = activity;
        this.executor = executor;
        this.newVersion = activity.getVersion() + 1;
        for (ActivityField activityField : activity.getFields()) {
            if(activityField.isIndicator() || activityField.isAttributeGroup()) {
                fieldMap.put(activityField.getFormField().getId(), activityField);
            }
        }
    }


    public void update(FormClass formClass) {
        updateActivityRow(formClass);
        updateDatabaseRow();
        updateFields(formClass);
    }

    private void updateActivityRow(FormClass formClass) {
        SqlUpdate activityUpdate = SqlUpdate.update("activity");
        activityUpdate.where("activityId", activity.getId());
        activityUpdate.setIfChanged("name", activity.getName(), formClass.getLabel(), 255);
        activityUpdate.set("formClass", formClass.toJsonString());
        activityUpdate.set("version", newVersion);
        activityUpdate.set("schemaVersion", newVersion);

        // TODO: location type
        activityUpdate.execute(executor);
    }


    private void updateDatabaseRow() {
        SqlUpdate databaseUpdate = SqlUpdate.update("userdatabase");
        databaseUpdate.where("databaseId", activity.getDatabaseId());
        databaseUpdate.set("version", System.currentTimeMillis());
        databaseUpdate.execute(executor);

    }

    private void updateFields(FormClass formClass) {
        int sortOrder = 1;
        
        Set<ResourceId> fieldsPresent = Sets.newHashSet();
        
        for (FormField formField : formClass.getFields()) {
            fieldsPresent.add(formField.getId());
            ActivityField existingField = fieldMap.get(formField.getId());
            if(existingField != null) {
                if (existingField.isIndicator()) {
                    updateIndicatorRow(existingField, formField, sortOrder);
                } else if(existingField.isAttributeGroup()) {
                    updateAttributeGroup(existingField, formField, sortOrder);
                }
            } else {
                if (isIndicator(formField)) {
                    insertIndicatorRow(formField, sortOrder);
                }
                if (isAttributeGroup(formField)) {
                    insertAttributeGroup(formField, sortOrder);
                }
            }
            sortOrder++;
        }

        for (ActivityField field : activity.getAttributeAndIndicatorFields()) {
            if(!fieldsPresent.contains(field.getFormField().getId())) {
                if(field.isIndicator()) {
                    deleteIndicator(field);
                } else if(field.isAttributeGroup()) {
                    deleteAttributeGroup(field);
                }
            }
        }
        
    }

    private boolean isAttributeGroup(FormField formField) {
        return formField.getId().getDomain() == CuidAdapter.ATTRIBUTE_GROUP_FIELD_DOMAIN;
    }

    private boolean isIndicator(FormField formField) {
        return formField.getId().getDomain() == CuidAdapter.INDICATOR_DOMAIN;
    }
    
    private void insertIndicatorRow(FormField formField, int sortOrder) {
        SqlInsert insert = SqlInsert.insertInto("indicator");
        insert.value("indicatorId", CuidAdapter.getLegacyIdFromCuid(formField.getId()));
        insert.value("activityId", activity.getId());
        insert.value("name", formField.getLabel());
        insert.value("nameInExpression", formField.getCode());
        insert.value("description", formField.getDescription());
        insert.value("aggregation", 0);
        insert.value("sortOrder", sortOrder);
        insert.value("type", formField.getType().getTypeClass().getId());
        insert.value("mandatory", formField.isRequired());
        
        if(formField.getType() instanceof QuantityType) {
            QuantityType quantityType = (QuantityType) formField.getType();
            insert.value("units", quantityType.getUnits());
        }
        if(formField.getType() instanceof CalculatedFieldType) {
            CalculatedFieldType type = (CalculatedFieldType) formField.getType();
            insert.value("calculatedAutomatically", true);
            insert.value("expression", type.getExpressionAsString());
        }
        insert.execute(executor);
    }


    private void deleteIndicator(ActivityField field) {
        SqlUpdate update = SqlUpdate.update("indicator");
        update.where("indicatorId", field.getId());
        update.set("dateDeleted", new Date());
        update.set("deleted", 1);
        update.execute(executor);
    }

    private void insertAttributeGroup(FormField formField, int groupSortOrder) {

        SqlInsert insert;

        int groupId = CuidAdapter.getLegacyIdFromCuid(formField.getId());
        EnumType type = (EnumType) formField.getType();

        insert = SqlInsert.insertInto("attributegroup");
        insert.value("AttributeGroupId", groupId);
        insert.value("multipleAllowed", type.getCardinality() == Cardinality.MULTIPLE);
        insert.value("sortOrder", groupSortOrder);
        insert.value("mandatory", formField.isRequired());
        insert.value("name", formField.getLabel(), 255);
        insert.execute(executor);
        
        insert = SqlInsert.insertInto("attributegroupinactivity");
        insert.value("attributeGroupId", groupId);
        insert.value("activityId", activity.getId());
        insert.execute(executor);

        int attributeSortOrder = 1;
        for (EnumItem enumItem : type.getValues()) {
            insert = SqlInsert.insertInto("attribute");
            insert.value("attributeId", CuidAdapter.getLegacyIdFromCuid(enumItem.getId()));
            insert.value("name", enumItem.getLabel(), 255);
            insert.value("attributeGroupId", groupId);
            insert.value("sortOrder", attributeSortOrder);
            insert.execute(executor);
            attributeSortOrder ++;
        }
    }


    private void updateIndicatorRow(ActivityField existingField, FormField formField, int sortOrder) {
        SqlUpdate update = SqlUpdate.update("indicator");
        update.where("indicatorId", existingField.getId());
        update.setIfChanged("name", existingField.getFormField().getLabel(), formField.getLabel());
        update.setIfChanged("nameInExpression", existingField.getFormField().getCode(), formField.getCode(), 255);
        update.setIfChanged("sortOrder", existingField.getSortOrder(), sortOrder);
        update.setIfChanged("description", existingField.getFormField().getDescription(), formField.getDescription());
        update.setIfChanged("mandatory", existingField.getFormField().isRequired(), formField.isRequired());

        if(existingField.getFormField().getType() instanceof QuantityType) {
            QuantityType existingType = (QuantityType) existingField.getFormField().getType();
            QuantityType updatedType = (QuantityType) formField.getType();
            update.setIfChanged("units", existingType.getUnits(), updatedType.getUnits(), 255);
        }

        if(existingField.getFormField().getType() instanceof CalculatedFieldType) {
            CalculatedFieldType existingType = (CalculatedFieldType) existingField.getFormField().getType();
            CalculatedFieldType updatedType = (CalculatedFieldType) formField.getType();
            update.setIfChanged("expression", existingType.getExpressionAsString(), updatedType.getExpressionAsString());
        }

        update.execute(executor);
    }
    
    private void updateAttributeGroup(ActivityField existingField, FormField formField, int sortOrder) {
        updateAttributeGroupRow(existingField, formField, sortOrder);
        updateAttributeRows(existingField, formField);
    }

    private void updateAttributeRows(ActivityField existingField, FormField formField) {
        EnumType existingType = (EnumType) existingField.getFormField().getType();
        EnumType updatedType = (EnumType) formField.getType();

        Map<ResourceId, EnumItem> existingItems = new HashMap<>();
        Set<ResourceId> presentItems = new HashSet<>();
        
        for (EnumItem enumItem : existingType.getValues()) {
            existingItems.put(enumItem.getId(), enumItem);
        }
        int sortOrder = 1;
        for (EnumItem updatedItem : updatedType.getValues()) {
            presentItems.add(updatedItem.getId());
            EnumItem existingItem = existingItems.get(updatedItem.getId());
            if(existingItem == null) {
                SqlInsert insert = SqlInsert.insertInto("attribute");
                insert.value("attributeId", CuidAdapter.getLegacyIdFromCuid(updatedItem.getId()));
                insert.value("name", updatedItem.getLabel(), 255);
                insert.value("sortOrder", sortOrder);
                insert.execute(executor);
            } else {
                SqlUpdate update = SqlUpdate.update("attribute");
                update.where("attributeId", CuidAdapter.getLegacyIdFromCuid(updatedItem.getId()));
                update.setIfChanged("name", existingItem.getLabel(), updatedItem.getLabel(), 255);
                update.setIfChanged("sortOrder", existingField.getSortOrder(), sortOrder);
                update.execute(executor);
            }
            sortOrder++;
        }

        for (EnumItem existingItem : existingType.getValues()) {
            if(!presentItems.contains(existingItem.getId())) {
                SqlUpdate delete = SqlUpdate.update("attribute");
                delete.where("attributeId", CuidAdapter.getLegacyIdFromCuid(existingItem.getId()));
                delete.set("dateDeleted", new Date());
                delete.set("deleted", 1);
                delete.execute(executor);
            }
        }
    }

    private void updateAttributeGroupRow(ActivityField existingField, FormField formField, int sortOrder) {
        SqlUpdate update = SqlUpdate.update("attributegroup");
        update.where("attributeGroupId", existingField.getId());
        update.setIfChanged("name", existingField.getFormField().getLabel(), formField.getLabel(), 255);
        update.setIfChanged("sortOrder", existingField.getSortOrder(), sortOrder);
        update.setIfChanged("mandatory", existingField.getFormField().isRequired(), formField.isRequired());
        
        update.execute(executor);
    }


    private void deleteAttributeGroup(ActivityField field) {
        SqlUpdate update = SqlUpdate.update("attributegroup");
        update.where("attributeGroupId", field.getId());
        update.set("dateDeleted", new Date());
        update.execute(executor);
    }
}
