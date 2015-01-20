package org.activityinfo.store.mysql.collections;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.service.store.ResourceNotFound;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Activity {

    public static final int REPORT_ONCE = 0;
    public static final int REPORT_MONTHLY = 1;

    private static final Logger LOGGER = Logger.getLogger(Activity.class.getName());

    private int activityId;
    private int databaseId;
    private int reportingFrequency;
    private int locationTypeId;
    private String category;
    private String locationTypeName;
    private int adminLevelId;

    private List<ActivityField> fields = Lists.newArrayList();


    public int getId() {
        return activityId;
    }

    public int getDatabaseId() {
        return databaseId;
    }

    public int getReportingFrequency() {
        return reportingFrequency;
    }

    public int getLocationTypeId() {
        return locationTypeId;
    }

    public String getCategory() {
        return category;
    }

    public String getLocationTypeName() {
        return locationTypeName;
    }

    public int getAdminLevelId() {
        return adminLevelId;
    }

    public List<ActivityField> getFields() {
        return fields;
    }
    
    public Iterable<ActivityField> getSiteFields() {
        if(reportingFrequency == REPORT_ONCE) {
            return fields;
        } else {
            return Iterables.filter(fields, new Predicate<ActivityField>() {
                @Override
                public boolean apply(ActivityField input) {
                    return !input.isAttributeGroup();
                }
            });
        }
    }

    public boolean hasLocationType() {
        // hack!!
        return !isNullLocationType();
    }

    private boolean isNullLocationType() {
        return "Country".equals(locationTypeName) && locationTypeId != 20301;
    }

    public ResourceId getProjectFormClassId() {
        return CuidAdapter.projectFormClass(databaseId);
    }
    public ResourceId getPartnerFormClassId() {
        return CuidAdapter.partnerFormClass(databaseId);
    }

    public ResourceId getLocationFormClassId() {
        return CuidAdapter.locationFormClass(locationTypeId);
    }

    public static Activity query(QueryExecutor executor, int activityId) throws SQLException {


        Activity activity = new Activity();
        activity.activityId = activityId;

        try (ResultSet rs = executor.query(
                "SELECT " +
                        "A.ActivityId, " +
                        "A.category, " +
                        "A.Name, " +
                        "A.ReportingFrequency, " +
                        "A.DatabaseId, " +
                        "A.LocationTypeId, " +
                        "L.Name locationTypeName, " +
                        "L.BoundAdminLevelId " +
                        "FROM activity A " +
                        "LEFT JOIN locationtype L on (A.locationtypeid=L.locationtypeid) " +
                        "LEFT JOIN userdatabase d on (A.databaseId=d.DatabaseId) " +
                        "WHERE A.ActivityId = " + activityId)) {

            if (!rs.next()) {
                throw new ResourceNotFound(CuidAdapter.activityFormClass(activityId));
            }

            activity.databaseId = rs.getInt("DatabaseId");
            activity.category = rs.getString("category");
            activity.reportingFrequency = rs.getInt("reportingFrequency");
            activity.locationTypeId = rs.getInt("locationTypeId");
            activity.locationTypeName = rs.getString("locationTypeName");
            activity.adminLevelId = rs.getInt("boundAdminLevelId");

        }

        activity.queryFields(executor);

        return activity;
    }

    private void queryFields(QueryExecutor executor) throws SQLException {

        Map<Integer, List<EnumItem>> attributes = queryAttributes(executor);

        String indicatorQuery = "(SELECT " +
                "ActivityId, " +
                "IndicatorId as Id, " +
                "Category, " +
                "Name, " +
                "Description, " +
                "Mandatory, " +
                "Type, " +
                "NULL as MultipleAllowed, " +
                "units, " +
                "SortOrder, " +
                "nameinexpression code, " +
                "calculatedautomatically ca, " +
                "Expression expr, " +
                "Aggregation aggregation " +
                "FROM indicator " +
                "WHERE dateDeleted IS NULL AND " +
                "ActivityId=" + activityId +
                " ) " +
                "UNION ALL " +
                "(SELECT " +
                "A.ActivityId, " +

                "G.attributeGroupId as Id, " +
                "NULL as Category, " +
                "Name, " +
                "NULL as Description, " +
                "Mandatory, " +
                "'ENUM' as Type, " +
                "multipleAllowed, " +
                "NULL as Units, " +
                "SortOrder, " +
                "NULL code, " +
                "NULL ca, " +
                "NULL expr, " +
                "NULL aggregation " +
                "FROM attributegroup G " +
                "INNER JOIN attributegroupinactivity A on G.attributeGroupId = A.attributeGroupId " +
                "WHERE dateDeleted is null AND " +
                "ActivityId=" + activityId +
                ") " +
                "ORDER BY SortOrder";

        try(ResultSet rs = executor.query(indicatorQuery)) {
            while(rs.next()) {
                addField(rs, attributes);
            }
        }
    }


    private void addField(ResultSet rs, Map<Integer, List<EnumItem>> attributes) throws SQLException {
        int id = rs.getInt("id");
        FormField formField;
        if(rs.getString("Type").equals("ENUM")) {
            formField = new FormField(CuidAdapter.attributeGroupField(id));
        } else {
            formField = new FormField(CuidAdapter.indicatorField(id));
        }
        formField.setLabel(rs.getString("Name"));
        formField.setRequired(getMandatory(rs));
        formField.setDescription(rs.getString("Description"));
        formField.setCode(rs.getString("code"));

        if(rs.getBoolean("ca") && rs.getString("expr") != null) {
            formField.setType(new CalculatedFieldType(rs.getString("expr")));

        } else {
            switch (rs.getString("Type")) {
                default:
                case "QUANTITY":
                    formField.setType(new QuantityType()
                            .setUnits(rs.getString("units")));
                    break;
                case "FREE_TEXT":
                    formField.setType(TextType.INSTANCE);
                    break;
                case "NARRATIVE":
                    formField.setType(TextType.INSTANCE);
                    break;
                case "ENUM":
                    formField.setType(createEnumType(rs, attributes));
                    break;
            }
        }

        fields.add(new ActivityField(id, rs.getString("category"), formField));
    }

    private boolean getMandatory(ResultSet rs) throws SQLException {
        try {
            return rs.getBoolean("Mandatory");
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE, "Exception while accessing mandatory flag (value = [" +
                    toDebugString(rs) + "]");
            throw new RuntimeException(e);
        }
    }

    private Object toDebugString(ResultSet rs) throws SQLException {
        try {
            Object object = rs.getObject("Mandatory");
            if(object == null) {
                return "null";
            } else {
                return object.toString() + ", class = " + object.getClass().getName();
            }
        } catch(Exception e) {
            return "Exception: " + e.getMessage();
        }
    }

    private EnumType createEnumType(ResultSet rs, Map<Integer, List<EnumItem>> attributes) throws SQLException {

        Cardinality cardinality;
        if(rs.getBoolean("multipleAllowed")) {
            cardinality = Cardinality.MULTIPLE;
        } else {
            cardinality = Cardinality.SINGLE;
        }

        List<EnumItem> enumValues = attributes.get(rs.getInt("id"));
        if(enumValues == null) {
            enumValues = Lists.newArrayList();
        }
        return new EnumType(cardinality, enumValues);
    }


    private Map<Integer, List<EnumItem>> queryAttributes(QueryExecutor executor) throws SQLException {

        Map<Integer, List<EnumItem>> attributes = Maps.newHashMap();

        String sql = "SELECT * " +
                "FROM attribute A " +
                "WHERE A.dateDeleted is null AND " +
                "AttributeGroupId in" +
                " (Select AttributeGroupId FROM attributegroupinactivity where ActivityId = " + activityId + ")" +
                " ORDER BY A.SortOrder";

        try(ResultSet rs = executor.query(sql)) {
            while(rs.next()) {
                int attributeGroupId = rs.getInt("AttributeGroupId");

                List<EnumItem> values = attributes.get(attributeGroupId);
                if(values == null) {
                    attributes.put(attributeGroupId, values = Lists.newArrayList());
                }

                int attributeId = rs.getInt("attributeId");
                String attributeName = rs.getString("name");

                values.add(new EnumItem(CuidAdapter.attributeId(attributeId), attributeName));
            }
        }

        return attributes;
    }


}
