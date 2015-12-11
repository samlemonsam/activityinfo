package org.activityinfo.store.mysql.metadata;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.NarrativeType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;


public class ActivityLoader {

    private static final Logger LOGGER = Logger.getLogger(ActivityLoader.class.getName());
    
    private final QueryExecutor executor;
    private Map<Integer, Activity> activityMap = new HashMap<>();

    public ActivityLoader(QueryExecutor executor) {
        this.executor = executor;
    }

    public Map<Integer, Activity> load(Set<Integer> activityIds) throws SQLException {

        if(!activityIds.isEmpty()) {
            FormClass serializedFormClass = null;

            Set<Integer> classicActivityIds = new HashSet<>();

            try (ResultSet rs = executor.query(
                    "SELECT " +
                            "A.ActivityId, " +
                            "A.category, " +
                            "A.Name, " +
                            "A.ReportingFrequency, " +
                            "A.DatabaseId, " +
                            "A.LocationTypeId, " +
                            "L.Name locationTypeName, " +
                            "L.BoundAdminLevelId, " +
                            "A.formClass, " +
                            "A.gzFormClass, " +
                            "d.ownerUserId, " +
                            "A.published, " +
                            "A.version " +
                            "FROM activity A " +
                            "LEFT JOIN locationtype L on (A.locationtypeid=L.locationtypeid) " +
                            "LEFT JOIN userdatabase d on (A.databaseId=d.DatabaseId) " +
                            "WHERE A.ActivityId IN " + idList(activityIds))) {

                while (rs.next()) {
                    Activity activity = new Activity();
                    activity.activityId = rs.getInt("ActivityId");
                    activity.databaseId = rs.getInt("DatabaseId");
                    activity.category = rs.getString("category");
                    activity.name = rs.getString("name");
                    activity.reportingFrequency = rs.getInt("reportingFrequency");
                    activity.locationTypeId = rs.getInt("locationTypeId");
                    activity.locationTypeName = rs.getString("locationTypeName");
                    activity.adminLevelId = rs.getInt("boundAdminLevelId");
                    activity.ownerUserId = rs.getInt("ownerUserId");
                    activity.published = rs.getInt("published") > 0;
                    activity.version = rs.getLong("version");

                    serializedFormClass = tryDeserialize(rs.getString("formClass"), rs.getBytes("gzFormClass"));

                    if (serializedFormClass == null) {
                        classicActivityIds.add(activity.getId());
                    } else {
                        addFields(activity, serializedFormClass);
                    }

                    activityMap.put(activity.getId(), activity);
                }
            }

            if (!classicActivityIds.isEmpty()) {
                loadFields(activityMap, classicActivityIds);
            }
        }
        return activityMap;
    }

    private String idList(Set<Integer> activityIds) {
        return "(" + Joiner.on(",").join(activityIds) + ")";
    }


    private static FormClass tryDeserialize(String formClass, byte[] formClassGz) {
        try {
            Reader reader;
            if (formClassGz != null) {
                reader = new InputStreamReader(new GZIPInputStream(new ByteArrayInputStream(formClassGz)));
            } else if (!Strings.isNullOrEmpty(formClass)) {
                reader = new StringReader(formClass);
            } else {
                return null;
            }

            Gson gson = new Gson();
            JsonObject object = gson.fromJson(reader, JsonObject.class);
            return FormClass.fromResource(Resources.resourceFromJson(object));
        } catch (IOException e) {
            throw new IllegalStateException("Error deserializing form class", e);
        }
    }

    private void addFields(Activity activity, FormClass formClass) {
        for (FormField formField : formClass.getFields()) {
            switch (formField.getId().getDomain()) {
                case CuidAdapter.ATTRIBUTE_GROUP_FIELD_DOMAIN:
                case CuidAdapter.INDICATOR_DOMAIN:
                    int fieldId = CuidAdapter.getLegacyIdFromCuid(formField.getId());
                    activity.fields.add(new ActivityField(fieldId, null, formField));
                    break;
            }
        }
    }

    private void loadFields(Map<Integer, Activity> activityMap, Set<Integer> activityIds) throws SQLException {

        Map<Integer, List<EnumItem>> attributes = loadAttributes(activityIds);

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
                "ActivityId IN " + idList(activityIds) +
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
                "ActivityId IN " + idList(activityIds) +
                ") " +
                "ORDER BY SortOrder";

        try(ResultSet rs = executor.query(indicatorQuery)) {
            while(rs.next()) {
                int activityId = rs.getInt(1);
                Activity activity = activityMap.get(activityId);
                
                addField(activity, rs, attributes);
            }
        }
    }

    private void addField(Activity activity, ResultSet rs, Map<Integer, List<EnumItem>> attributes) throws SQLException {
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
                    formField.setType(NarrativeType.INSTANCE);
                    break;
                case "ENUM":
                    formField.setType(createEnumType(rs, attributes));
                    break;
            }
        }

        activity.fields.add(new ActivityField(id, rs.getString("category"), formField));
    }

    private boolean getMandatory(ResultSet rs) throws SQLException {
        return rs.getBoolean("Mandatory");
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


    private Map<Integer, List<EnumItem>> loadAttributes(Set<Integer> activityIds) throws SQLException {

        Map<Integer, List<EnumItem>> attributes = Maps.newHashMap();

        String sql = "SELECT * " +
                "FROM attribute A " +
                "WHERE A.dateDeleted is null AND " +
                "AttributeGroupId in" +
                " (Select AttributeGroupId FROM attributegroupinactivity where ActivityId IN " + idList(activityIds) + ")" +
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
