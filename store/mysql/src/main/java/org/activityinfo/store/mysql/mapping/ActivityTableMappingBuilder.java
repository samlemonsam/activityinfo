package org.activityinfo.store.mysql.mapping;

import com.google.common.collect.Lists;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormElement;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.NarrativeType;
import org.activityinfo.model.type.NarrativeValue;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.time.LocalDateType;
import org.activityinfo.store.mysql.Join;
import org.activityinfo.store.mysql.metadata.Activity;
import org.activityinfo.store.mysql.metadata.ActivityField;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.activityinfo.model.legacy.CuidAdapter.*;


public class ActivityTableMappingBuilder {

    public static final int REPORT_ONCE = 0;
    public static final int REPORT_MONTHLY = 1;

    
    private static final Join PERIOD_JOIN = new Join("period", 
            "LEFT JOIN reportingperiod period ON (period.SiteId=base.siteId)");

    private Activity activity;
    private ResourceId classId;
    
    private String baseFromClause;
    private String baseFilter;
    private String baseTable;
    private FormClass formClass;
    private List<FieldMapping> mappings = Lists.newArrayList();
    private PrimaryKeyMapping primaryKeyMapping;


    public ActivityTableMappingBuilder() {
    }

    public static ActivityTableMappingBuilder site(Activity activity) {
        
        if(activity.getSerializedFormClass() != null) {
            return newForm(activity);
        }
        
        ActivityTableMappingBuilder mapping = new ActivityTableMappingBuilder();
        mapping.activity = activity;
        mapping.baseTable = "site";
        mapping.baseFromClause = "site base";
        mapping.baseFilter = "base.deleted=0     AND base.activityId=" + activity.getId();
        mapping.classId = CuidAdapter.activityFormClass(activity.getId());
        mapping.formClass = new FormClass(mapping.classId);
        mapping.formClass.setLabel(activity.getName());
        mapping.formClass.setDatabaseId(activity.getDatabaseId());
        mapping.primaryKeyMapping = new PrimaryKeyMapping(CuidAdapter.SITE_DOMAIN, "siteId");

        if(activity.getReportingFrequency() == Activity.REPORT_ONCE) {
            mapping.addDateFields();
        }

        mapping.addPartnerField();
        mapping.addProjectField();

        if(activity.hasLocationType()) {
            mapping.addLocationField();
        }

        for(ActivityField field : activity.getAttributeAndIndicatorFields()) {
            mapping.addIndicatorOrAttributeField(field);
        }
        
        mapping.addComments();

        sortFormClassFields(mapping.formClass, activity.getFieldsOrder());

        return mapping;
    }

    private static ActivityTableMappingBuilder newForm(Activity activity) {
        ActivityTableMappingBuilder mapping = new ActivityTableMappingBuilder();
        mapping.activity = activity;
        mapping.baseTable = "site";
        mapping.baseFromClause = "site base";
        mapping.baseFilter = "base.deleted=0     AND base.activityId=" + activity.getId();
        mapping.classId = CuidAdapter.activityFormClass(activity.getId());
        mapping.formClass = activity.getSerializedFormClass();
        mapping.primaryKeyMapping = new PrimaryKeyMapping(CuidAdapter.SITE_DOMAIN, "siteId");

        // Add mappings between fields and the base 'site' table, if they are present in the form class.
        for (FormField formField : mapping.formClass.getFields()) {
            if(formField.getId().equals(CuidAdapter.field(mapping.classId, CuidAdapter.START_DATE_FIELD))) {
                mapping.addStartDateMapping(formField);
            
            } else if (formField.getId().equals(CuidAdapter.field(mapping.classId, CuidAdapter.END_DATE_FIELD))) {
                mapping.addEndDateMapping(formField);

            } else if (formField.getId().equals(CuidAdapter.field(mapping.classId, CuidAdapter.PARTNER_FIELD))) {
                mapping.addPartnerField(formField);

            } else if (formField.getId().equals(CuidAdapter.field(mapping.classId, CuidAdapter.PROJECT_FIELD))) {
                mapping.addProjectField(formField);
            
            } else if (formField.getId().equals(CuidAdapter.field(mapping.classId, CuidAdapter.COMMENT_FIELD))) {
                mapping.addComments(formField);
            }
        }
        return mapping;
    }

    public static ActivityTableMappingBuilder reportingPeriod(Activity activity) {
        ActivityTableMappingBuilder mapping = new ActivityTableMappingBuilder();
        mapping.activity = activity;
        mapping.baseTable = "reportingperiod";
        mapping.baseFromClause = "reportingperiod base LEFT JOIN site on (site.siteId=base.siteId)";
        mapping.baseFilter = "site.deleted=0 AND site.activityId=" + activity.getId();
        mapping.classId = CuidAdapter.reportingPeriodFormClass(activity.getId());
        mapping.formClass = new FormClass(mapping.classId);
        mapping.formClass.setLabel(activity.getName() + " Monthly Reports");
        mapping.formClass.setDatabaseId(activity.getDatabaseId());
        mapping.primaryKeyMapping = new PrimaryKeyMapping(CuidAdapter.MONTHLY_REPORT, "reportingPeriodId");
        
        mapping.addSiteField();
        mapping.addDateFields();

        for (ActivityField indicatorField : activity.getIndicatorFields()) {
            mapping.addIndicatorOrAttributeField(indicatorField);
        }

        sortFormClassFields(mapping.formClass, activity.getFieldsOrder());
        
        return mapping;
    }

    private static void sortFormClassFields(FormClass formClass, final Map<ResourceId, Integer> sortMap) {
        Collections.sort(formClass.getElements(), new Comparator<FormElement>() {
            @Override
            public int compare(FormElement o1, FormElement o2) {
                Integer c1 = sortMap.get(o1.getId());
                Integer c2 = sortMap.get(o2.getId());
                return c1 != null && c2 != null ? c1.compareTo(c2) : 0;
            }
        });
    }

    public void addDateFields() {
        FormField date1 = new FormField(field(classId, START_DATE_FIELD))
                .setLabel("Start Date")
                .setCode("date1")
                .setType(LocalDateType.INSTANCE)
                .setRequired(true);
        formClass.addElement(date1);
        addStartDateMapping(date1);

        FormField date2 = new FormField(field(classId, END_DATE_FIELD))
                .setLabel("End Date")
                .setCode("date2")
                .setType(LocalDateType.INSTANCE)
                .setRequired(true);
        formClass.addElement(date2);
        addEndDateMapping(date2);
    }

    private void addStartDateMapping(FormField date1) {
        mappings.add(new FieldMapping(date1, "date1", DateConverter.INSTANCE));
    }

    private void addEndDateMapping(FormField date2) {
        mappings.add(new FieldMapping(date2, "date2", DateConverter.INSTANCE));
    }

    public void addSiteField() {
        FormField siteField = new FormField(field(classId, SITE_FIELD));
        siteField.setLabel("Site");
        siteField.setCode("site");
        siteField.setType(ReferenceType.single(CuidAdapter.activityFormClass(activity.getId())));
        siteField.setRequired(true);
        
        formClass.addElement(siteField);
        mappings.add(new FieldMapping(siteField, "siteId", new ReferenceConverter(SITE_DOMAIN)));
    }
    
    public void addLocationField() {
        FormField locationField = new FormField(field(classId, LOCATION_FIELD));
        locationField.setLabel(activity.getLocationTypeName());
        locationField.setCode("location");
        locationField.setType(ReferenceType.single(activity.getLocationFormClassIds()));
        locationField.setRequired(true);
        
        formClass.addElement(locationField);
        mappings.add(new FieldMapping(locationField, "locationId", new ReferenceConverter(LOCATION_DOMAIN)));
    }

    public void addPartnerField() {

        FormField partnerField = new FormField(field(classId, PARTNER_FIELD))
                .setLabel("Partner")
                .setCode("partner")
                .setType(ReferenceType.single(activity.getPartnerFormClassId()))
                .setRequired(true);
        formClass.addElement(partnerField);
        addPartnerField(partnerField);
    }

    private void addPartnerField(FormField partnerField) {
        mappings.add(new FieldMapping(partnerField, "partnerId", new ReferenceConverter(PARTNER_DOMAIN)));
    }

    public void addProjectField() {
        FormField projectField = new FormField(field(classId, PROJECT_FIELD))
                .setLabel("Project")
                .setCode("project")
                .setType(ReferenceType.single(activity.getProjectFormClassId()))
                .setRequired(false);
        formClass.addElement(projectField);
        addProjectField(projectField);
    }

    private void addProjectField(FormField projectField) {
        mappings.add(new FieldMapping(projectField, "projectId", new ReferenceConverter(PROJECT_DOMAIN)));
    }

    public void addComments(){
        FormField commentsField = new FormField(field(classId, COMMENT_FIELD))
                .setLabel("Comments")
                .setCode("comments")
                .setType(NarrativeType.INSTANCE)
                .setRequired(false);
        
        formClass.addElement(commentsField);
        addComments(commentsField);
    }

    private void addComments(FormField commentsField) {
        mappings.add(new FieldMapping(commentsField, "comments", new FieldValueConverter() {
            @Override
            public FieldValue toFieldValue(ResultSet rs, int index) throws SQLException {
                return NarrativeValue.valueOf(rs.getString(index));
            }

            @Override
            public Collection<?> toParameters(FieldValue value) {
                if(value instanceof NarrativeValue) {
                    return Collections.singleton(((NarrativeValue) value).asString());
                } else {
                    return Collections.singleton(null);
                }   
            }
        }));
    }

    public TableMapping build() {
        return new TableMapping("site", baseFromClause, baseFilter, primaryKeyMapping, mappings, formClass,
                DeleteMethod.SOFT_BY_DATE_AND_BOOLEAN, Collections.<String, Object>emptyMap(), 0L);
    }

    public void addIndicatorOrAttributeField(ActivityField field) {
        formClass.addElement(field.getFormField());
    }

}
