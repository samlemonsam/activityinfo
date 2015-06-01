package org.activityinfo.store.mysql.mapping;

import com.google.common.collect.Lists;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.time.LocalDateType;
import org.activityinfo.store.mysql.Join;
import org.activityinfo.store.mysql.collections.Activity;
import org.activityinfo.store.mysql.collections.ActivityField;

import java.util.List;

import static org.activityinfo.model.legacy.CuidAdapter.*;


public class ActivityTableMappingBuilder {

    public static final int REPORT_ONCE = 0;
    public static final int REPORT_MONTHLY = 1;

    
    private static final Join PERIOD_JOIN = new Join("period", 
            "LEFT JOIN reportingperiod period ON (period.SiteId=base.siteId)");

    private Activity activity;
    private ResourceId classId;
    
    private String tableName;
    private String baseFilter;
    private FormClass formClass;
    private List<FieldMapping> mappings = Lists.newArrayList();
    private PrimaryKeyMapping primaryKeyMapping;


    public ActivityTableMappingBuilder() {
    }

    public static ActivityTableMappingBuilder site(Activity activity) {
        ActivityTableMappingBuilder mapping = new ActivityTableMappingBuilder();
        mapping.activity = activity;
        mapping.tableName = "site";
        mapping.baseFilter = "base.activityId=" + activity.getId();
        mapping.classId = CuidAdapter.activityFormClass(activity.getId());
        mapping.formClass = new FormClass(mapping.classId);
        mapping.formClass.setLabel(activity.getName());
        mapping.formClass.setOwnerId(CuidAdapter.databaseId(activity.getDatabaseId()));
        mapping.primaryKeyMapping = new PrimaryKeyMapping(CuidAdapter.SITE_DOMAIN, "siteId");

        if(activity.getReportingFrequency() == Activity.REPORT_ONCE) {
            mapping.addDateFields();
        }

        mapping.addPartnerField();
    //    mapping.addProjectField();

        if(activity.hasLocationType()) {
            mapping.addLocationField();
        }

        for(ActivityField field : activity.getSiteFields()) {
            mapping.addSiteField(field);
        }

        return mapping;
    }

    public static ActivityTableMappingBuilder reportingPeriod(int activityId) {
        ActivityTableMappingBuilder mapping = new ActivityTableMappingBuilder();
        mapping.tableName = "reportingperiod base LEFT JOIN site on (site.siteId=base.siteId)";
        mapping.baseFilter = "site.activityId=" + activityId;
        mapping.classId = CuidAdapter.reportingPeriodFormClass(activityId);
        mapping.formClass = new FormClass(mapping.classId);
        mapping.primaryKeyMapping = new PrimaryKeyMapping(CuidAdapter.MONTHLY_REPORT, "base.reportingPeriodId");
        return mapping;
    }


    public void addDateFields() {
        FormField date1 = new FormField(field(classId, START_DATE_FIELD))
                .setLabel("Start Date")
                .setCode("date1")
                .setType(LocalDateType.INSTANCE)
                .setRequired(true);
        formClass.addElement(date1);
        mappings.add(new FieldMapping(date1, "date1", Extractor.DATE));

        FormField date2 = new FormField(field(classId, END_DATE_FIELD))
                .setLabel("End Date")
                .setCode("date2")
                .setType(LocalDateType.INSTANCE)
                .setRequired(true);
        formClass.addElement(date2);
        mappings.add(new FieldMapping(date2, "date2", Extractor.DATE));
    }
    
    public void addLocationField() {
        FormField locationField = new FormField(field(classId, LOCATION_FIELD));
        locationField.setLabel(activity.getLocationTypeName());
        locationField.setCode("location");
        locationField.setType(ReferenceType.single(activity.getLocationFormClassId()));
        locationField.setRequired(true);
        
        formClass.addElement(locationField);
        mappings.add(new FieldMapping(locationField, "locationId", new ForeignKeyExtractor(LOCATION_DOMAIN)));
    }

    public void addPartnerField() {

        FormField partnerField = new FormField(field(classId, PARTNER_FIELD))
                .setLabel("Partner")
                .setCode("partner")
                .setType(ReferenceType.single(CuidAdapter.partnerFormClass(activity.getDatabaseId())))
                .setRequired(true);
        formClass.addElement(partnerField);
        mappings.add(new FieldMapping(partnerField, "partnerId", new ForeignKeyExtractor(PARTNER_DOMAIN)));
    }

    public void addProjectField() {
        FormField partnerField = new FormField(field(classId, PARTNER_FIELD))
                .setLabel("Project")
                .setCode("project")
                .setType(ReferenceType.single(activity.getProjectFormClassId()))
                .setRequired(false);
        formClass.addElement(partnerField);
        mappings.add(new FieldMapping(partnerField, "projectId", new ForeignKeyExtractor(PROJECT_DOMAIN)));
    }
    

    public TableMapping build() {
        return new TableMapping(tableName, baseFilter, primaryKeyMapping, mappings, formClass);
    }

    public void addSiteField(ActivityField field) {
        formClass.addElement(field.getFormField());
    }

}
