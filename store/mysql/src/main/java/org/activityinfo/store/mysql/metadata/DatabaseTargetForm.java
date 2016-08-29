package org.activityinfo.store.mysql.metadata;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.time.LocalDateType;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.mapping.TableMappingBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class DatabaseTargetForm implements Serializable {


    private int databaseId;
    private String databaseName;
    private List<FormField> indicatorFields = Lists.newArrayList();
    private Map<Integer, FormField> fieldMap = Maps.newHashMap();
    
    public DatabaseTargetForm(int databaseId, String databaseName) {
        this.databaseId = databaseId;
        this.databaseName = databaseName;
    }

    public void addIndicator(int id, String name, String units) {
        FormField field = new FormField(CuidAdapter.cuid(CuidAdapter.TARGET_INDICATOR_FIELD_DOMAIN, id));
        field.setLabel(name);
        field.setSuperProperty(CuidAdapter.indicatorField(id));
        field.setType(new QuantityType(units));
        indicatorFields.add(field);
        fieldMap.put(id, field);
    }

    public ResourceId getFormClassId() {
        return CuidAdapter.cuid(CuidAdapter.TARGET_FORM_CLASS_DOMAIN, databaseId);
    }

    public int getDatabaseId() {
        return databaseId;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public List<FormField> getIndicatorFields() {
        return indicatorFields;
    }

    public FormField getIndicatorField(int indicatorId) {
        return fieldMap.get(indicatorId);
    }
    
    public TableMapping buildMapping() {
        ResourceId classId = getFormClassId();
        
        TableMappingBuilder mapping = TableMappingBuilder.newMapping(classId, "target");
        mapping.setFormLabel(databaseName + " Targets");
        mapping.setFromClause("target base");
        mapping.setBaseFilter("databaseId = " + databaseId);
        mapping.setDatabaseId(CuidAdapter.databaseId(databaseId));
        mapping.setPrimaryKeyMapping(CuidAdapter.TARGET_INSTANCE_DOMAIN, "TargetId");
        
        FormField nameField = new FormField(CuidAdapter.field(classId, CuidAdapter.NAME_FIELD));
        nameField.setCode("name");
        nameField.setLabel(I18N.CONSTANTS.name());
        nameField.setType(TextType.INSTANCE);
        nameField.setRequired(true);
        
        FormField startDateField = new FormField(CuidAdapter.field(classId, CuidAdapter.START_DATE_FIELD));
        startDateField.setLabel(I18N.CONSTANTS.startDate());
        startDateField.setCode("fromDate");
        startDateField.setType(LocalDateType.INSTANCE);

        FormField endDateField = new FormField(CuidAdapter.field(classId, CuidAdapter.END_DATE_FIELD));
        endDateField.setLabel(I18N.CONSTANTS.endDate());
        endDateField.setCode("toDate");
        endDateField.setType(LocalDateType.INSTANCE);

        FormField partnerField = new FormField(CuidAdapter.field(classId, CuidAdapter.PARTNER_FIELD));
        partnerField.setLabel(I18N.CONSTANTS.partner());
        partnerField.setCode("partner");
        partnerField.setType(ReferenceType.single(CuidAdapter.partnerFormClass(databaseId)));
        
        FormField projectField = new FormField(CuidAdapter.field(classId, CuidAdapter.PROJECT_FIELD));
        projectField.setLabel(I18N.CONSTANTS.project());
        projectField.setCode("project");
        projectField.setType(ReferenceType.single(CuidAdapter.projectFormClass(databaseId)));
        
        mapping.addTextField(nameField, "name");
        mapping.addDateField(startDateField, "date1");
        mapping.addDateField(endDateField, "date2");
        mapping.addReferenceField(partnerField, CuidAdapter.PARTNER_DOMAIN, "partnerId");
        mapping.addReferenceField(projectField, CuidAdapter.PROJECT_DOMAIN, "projectId");

        for (FormField indicatorField : indicatorFields) {
            mapping.addUnmappedField(indicatorField);
        }
        
        return mapping.build();
    }
}
