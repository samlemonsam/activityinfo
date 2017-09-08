package org.activityinfo.store.mysql.metadata;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.cloud.sql.jdbc.internal.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.NarrativeType;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.barcode.BarcodeType;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;


/**
 * Loads metadata about Activities from MySQL.
 *
 */
public class ActivityLoader {

    private static final Logger LOGGER = Logger.getLogger(ActivityLoader.class.getName());

    private final MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();
    private final QueryExecutor executor;
    private final ParentKeyCache parentKeys;
    private final PermissionsCache permissionCache;

    /**
     * Cache for activity metadata for the duration of a request.
     */
    private Map<Integer, Activity> activityMap = new HashMap<>();

    public ActivityLoader(QueryExecutor executor) {
        this.executor = executor;
        this.parentKeys = new ParentKeyCache(executor);
        this.permissionCache = new PermissionsCache(executor);
    }
    
    public Map<Integer, Activity> loadForIndicators(Set<Integer> indicatorIds) throws SQLException {

        Set<Integer> distinctActivityIds = new HashSet<>(
                parentKeys.lookupActivityByIndicator(indicatorIds).values());
        
        return load(distinctActivityIds);
    }
    
    public Map<Integer, Activity> loadForDatabaseIds(Set<Integer> databaseIds) throws SQLException {
        return load(parentKeys.queryActivitiesForDatabase(databaseIds));
    }

    public PermissionsCache getPermissionCache() {
        return permissionCache;
    }
    
    public UserPermission getPermission(int activityId, int userId) throws SQLException {
        Activity activity = load(activityId);
        if(activity.isPublished()) {
            return UserPermission.viewAll();
        }
        if(activity.getOwnerUserId() == userId) {
            return UserPermission.viewAll();
        }
        return permissionCache.getPermission(userId, activity.getDatabaseId());
    }

    public Activity load(int activityId) throws SQLException {
        if(activityMap.containsKey(activityId)) {
            return activityMap.get(activityId);
        }

        Map<Integer, Activity> result = load(Collections.singleton(activityId));
        if(!result.containsKey(activityId)) {
            throw new IllegalArgumentException("No such activity " + activityId);
        }
        
        return result.get(activityId);
    }

    public Map<Integer, Activity> load(Set<Integer> activityIds) throws SQLException {
        
        Map<Integer, Activity> loaded = Maps.newHashMap();
        
        // first see what has already been loaded during this transaction
        for (Integer activityId : activityIds) {
            if(activityMap.containsKey(activityId)) {
                loaded.put(activityId, activityMap.get(activityId));
            }
        }

        // For any remaining activities, we need to hit the database to get the latest 
        // schema version and data version
        // This should be super fast
        Set<Integer> toFetch = Sets.newHashSet(Sets.difference(activityIds, loaded.keySet()));
        if(!toFetch.isEmpty()) {
            List<ActivityVersion> versions = queryVersions(toFetch);

            // Retrieve the schemas that we can from memcache using the schemaCacheKeys
            Map<Integer, Activity> cached = loadFromMemcache(versions);
            loaded.putAll(cached);
            activityMap.putAll(cached);
            toFetch.removeAll(cached.keySet());

            // If anything remains, need to hit the database
            // <sigh>
            if(!toFetch.isEmpty()) {
                Map<Integer, Activity> fetched = loadFromMySql(activityIds);
                loaded.putAll(fetched);
                cacheToMemcache(fetched);
            }
        }

        return loaded;
    }

    /**
     * Queries the current versions of each of given activities from the MySQL database.
     * 
     * <p>We use these versions number as part of our memcache keys, ensuring that we 
     * always get data from the cache that is consistent with the current transaction.</p>
     */
    private List<ActivityVersion> queryVersions(Set<Integer> activityId) throws SQLException {
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT activityid, schemaVersion, siteVersion FROM activity WHERE activityId IN (");
        Joiner.on(',').appendTo(sql, activityId);
        sql.append(")");
        
        List<ActivityVersion> versions = Lists.newArrayList();
        
        try(ResultSet rs = executor.query(sql.toString())) {
            while(rs.next()) {
                versions.add(new ActivityVersion(
                        rs.getInt(1),    // activityId
                        rs.getLong(2),   // schema version
                        rs.getLong(3))); // site (data) versions
            }
        }
        return versions;
    }

    /**
     * Loads a set of activity schemas from the cache.
     */
    private Map<Integer, Activity> loadFromMemcache(List<ActivityVersion> activityVersions) {
        Map<Integer, Activity> loaded = new HashMap<>();
        try {
            Set<String> memcacheKeys = Sets.newHashSet();
            for (ActivityVersion activity : activityVersions) {
                memcacheKeys.add(activity.getSchemaCacheKey());
            }
            Map<String, Object> cached = memcacheService.getAll(memcacheKeys);
            for (ActivityVersion activityVersion : activityVersions) {
                Activity activity = (Activity) cached.get(activityVersion.getSchemaCacheKey());
                
                if(activity != null) {
                    
                    // Update the cached activity with the data version number,
                    // which is version seperately from the schema itself.
                    activity.siteVersion = activityVersion.getSiteVersion();
                    
                    loaded.put(activityVersion.getId(), activity);
                }
            }
        } catch (Exception e) {
            // Log but otherwise ignore memcache failure
            LOGGER.log(Level.SEVERE, "Exception loading activities from memcache", e);
        }
        return loaded;
    }


    private void cacheToMemcache(Map<Integer, Activity> loadedFromDatabase) {
        try {
            Map<String, Activity> toCache = new HashMap<>();
            for (Activity activity : loadedFromDatabase.values()) {
                toCache.put(activity.getActivityVersion().getSchemaCacheKey(), activity);                
            }
            memcacheService.putAll(toCache);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception caching activities to memcache", e);
        }
    }

    private Map<Integer, Activity> loadFromMySql(Set<Integer> activityIds) throws SQLException {

        LOGGER.fine("Loading " + activityIds.size() + " activities...");
        
        if(!activityIds.isEmpty()) {

            Set<Integer> classicActivityIds = new HashSet<>();

            try (ResultSet rs = executor.query(
                    "SELECT " +
                            "A.ActivityId, " +              // (1)
                            "A.category, " +                // (2)
                            "A.Name, " +                    // (3)
                            "A.ReportingFrequency, " +      // (4)
                            "A.SortOrder, " +               // (5)
                            "A.DatabaseId, " +              // (6)
                            "d.Name, " +                    // (7)
                            "A.LocationTypeId, " +          // (8)
                            "L.Name locationTypeName, " +   // (9)
                            "L.BoundAdminLevelId, " +       // (10)
                            "A.formClass, " +               // (11)
                            "A.gzFormClass, " +             // (12)
                            "d.ownerUserId, " +             // (13)
                            "A.published, " +               // (14)
                            "A.siteVersion, " +             // (15)
                            "A.schemaVersion, " +           // (16)
                            "(A.dateDeleted IS NOT NULL OR " +
                            " d.dateDeleted IS NOT NULL), " + // (17)
                            "d.ownerUserId, " +               // (18)
                            "A.classicView " +                // (19)  
                            "FROM activity A " +    
                            "LEFT JOIN locationtype L on (A.locationtypeid=L.locationtypeid) " +
                            "LEFT JOIN userdatabase d on (A.databaseId=d.DatabaseId) " +
                            "WHERE A.ActivityId IN " + idList(activityIds))) {

                while (rs.next()) {
                    Activity activity = new Activity();
                    activity.activityId = rs.getInt(1);
                    activity.category = rs.getString(2);
                    activity.name = rs.getString(3);
                    activity.reportingFrequency = rs.getInt(4);
                    activity.sortOrder = rs.getInt(5);
                    activity.databaseId = rs.getInt(6);
                    activity.databaseName = rs.getString(7);
                    activity.locationTypeId = rs.getInt(8);
                    activity.locationTypeName = rs.getString(9);
                    activity.adminLevelId = rs.getInt(10);
                    if(rs.wasNull()) {
                        activity.adminLevelId = null;
                    }
                    activity.ownerUserId = rs.getInt(13);
                    activity.published = rs.getInt(14) > 0;
                    activity.siteVersion = rs.getLong(15);
                    activity.schemaVersion = rs.getLong(16);
                    activity.deleted = rs.getBoolean(17);
                    activity.ownerUserId = rs.getInt(18);


                    activity.locationTypeIds.add(activity.locationTypeId);
                    if(activity.adminLevelId == null) {
                        activity.locationRange.add(CuidAdapter.locationFormClass(activity.locationTypeId));
                    } else {
                        activity.locationRange.add(CuidAdapter.adminLevelFormClass(activity.adminLevelId));
                    }

                    // Only use json form for forms that are explicitly non-classicView,
                    // otherwise we miss aggregation method.
                    activity.classicView = rs.getBoolean(19);
                    
                    FormClass serializedFormClass = null;
                    if(!activity.classicView) {
                        serializedFormClass = tryDeserialize(activity, rs.getString("formClass"), rs.getBytes("gzFormClass"));
                    }
                    if (serializedFormClass == null) {
                        classicActivityIds.add(activity.getId());
                    } else {
                        addFields(activity, serializedFormClass);
                        activity.serializedFormClass.value = serializedFormClass;
                    }

                    activityMap.put(activity.getId(), activity);
                }
            }

            /*
             * Because we allow users to change location types,
             * a location field might actually reference several different location types.
             * These need to be included in the range so that queries can be correctly run
             */
            try (ResultSet rs = executor.query(
                    "SELECT DISTINCT S.ActivityId, L.LocationTypeId, T.BoundAdminLevelId " +
                            "FROM site S " +
                            "LEFT JOIN location L on (S.locationId=L.locationId) " +
                            "LEFT JOIN locationtype T on (L.locationTypeId=T.LocationTypeId) " +
                            "WHERE S.ActivityId IN " + idList(activityIds))) {

                while(rs.next()) {
                    int activityId = rs.getInt(1);
                    Activity activity = activityMap.get(activityId);

                    int locationTypeId =  rs.getInt(2);
                    activity.locationTypeIds.add(locationTypeId);

                    int adminLevelId = rs.getInt(3);
                    ResourceId formId;
                    if(rs.wasNull()) {
                        formId = CuidAdapter.locationFormClass(locationTypeId);
                    } else {
                        formId = CuidAdapter.adminLevelFormClass(adminLevelId);
                    }
                    if(!activity.locationRange.contains(formId)) {
                        activity.locationRange.add(formId);
                    }
                }
            }

            /*
             * For those activities which are not serialized as json, load the list of indicators/attributes now
             */
            if (!classicActivityIds.isEmpty()) {
                loadFields(activityMap, classicActivityIds);
            }
            
            /*
             * Finally we need metadata on linked indicators here.
             */
            loadLinkedIndicators(activityMap, activityIds);
            
        }
        return activityMap;
    }


    private String idList(Set<Integer> activityIds) {
        return "(" + Joiner.on(",").join(activityIds) + ")";
    }


    private static FormClass tryDeserialize(Activity activity, String formClass, byte[] formClassGz) {
        try {
            Reader reader;
            if (formClassGz != null) {
                reader = new InputStreamReader(new GZIPInputStream(new ByteArrayInputStream(formClassGz)), Charsets.UTF_8);
            } else if (!Strings.isNullOrEmpty(formClass)) {
                reader = new StringReader(formClass);
            } else {
                return null;
            }

            Gson gson = new Gson();
            JsonObject object = gson.fromJson(reader, JsonObject.class);
            return patchDeserializedFormClass(activity, FormClass.fromJson(object));
        } catch (IOException e) {
            throw new IllegalStateException("Error deserializing form class", e);
        }
    }

    /**
     * Apply any updates to the serialized FormClass that might be required to do changes in
     * ActivityInfo.
     */
    private static FormClass patchDeserializedFormClass(Activity activity, FormClass formClass) {

        // Ensure that all forms have the database id
        formClass.setDatabaseId(activity.getDatabaseId());

        // Some partner fields have been stored to the JSON as pointing to the wrong database,
        // either because there was a bug in the past or because the databaseId was manually updated
        // in the activity table without a corresponding change to the formClass field.
        ResourceId partnerFieldId = CuidAdapter.partnerField(activity.getId());
        ReferenceType expectedPartnerType = ReferenceType.single(CuidAdapter.partnerFormId(activity.getDatabaseId()));

        Optional<FormField> partnerField = formClass.getFieldIfPresent(partnerFieldId);
        if(!partnerField.isPresent()) {
            FormField newPartnerField = new FormField(partnerFieldId);
            newPartnerField.setType(expectedPartnerType);
            newPartnerField.setVisible(true);
            newPartnerField.setRequired(true);
            newPartnerField.setLabel("Partner");
            formClass.addElement(newPartnerField);

        } else {
            partnerField.get().setType(expectedPartnerType);
        }

        // The (classic) pivot table components rely on a fixed fields with date1 and date2
        ResourceId startDateId = CuidAdapter.field(formClass.getId(), CuidAdapter.START_DATE_FIELD);
        ResourceId endDateId = CuidAdapter.field(formClass.getId(), CuidAdapter.END_DATE_FIELD);

        for (FormField field : formClass.getFields()) {
            if(field.getId().equals(startDateId) && Strings.isNullOrEmpty(field.getCode())) {
                field.setCode("date1");
            }
            if(field.getId().equals(endDateId) && Strings.isNullOrEmpty(field.getCode())) {
                field.setCode("date2");
            }
        }
        return formClass;
    }

    private void addFields(Activity activity, FormClass formClass) {
        int sortOrder = 1;
        for (FormField formField : formClass.getFields()) {
            activity.fieldsOrder.put(formField.getId(), sortOrder); // include also built-in fields
            switch (formField.getId().getDomain()) {
                case CuidAdapter.ATTRIBUTE_GROUP_FIELD_DOMAIN:
                case CuidAdapter.INDICATOR_DOMAIN:
                    int fieldId = CuidAdapter.getLegacyIdFromCuid(formField.getId());
                    activity.fields.add(new ActivityField(fieldId, null, formField, sortOrder++));
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
                "WHERE deleted = 0 AND " +
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
                            .setUnits(rs.getString("units"))
                            .setAggregation(rs.getInt("Aggregation")));
                    break;
                case "BARCODE":
                    formField.setType(BarcodeType.INSTANCE);
                    break;
                
                case "FREE_TEXT":
                    formField.setType(TextType.SIMPLE);
                    break;
                case "NARRATIVE":
                    formField.setType(NarrativeType.INSTANCE);
                    break;
                case "ENUM":
                    formField.setType(createEnumType(rs, attributes));
                    break;
            }
        }
        ActivityField field = new ActivityField(id, rs.getString("category"), formField, rs.getInt("sortOrder"));
        field.sortOrder = rs.getInt("sortOrder");
        
        activity.fields.add(field);
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

        String sql = "SELECT DISTINCT A.* from attribute A " +
                "LEFT JOIN attributegroupinactivity G on (A.attributegroupid=G.attributegroupid) " +
                "WHERE G.activityid IN " + idList(activityIds) + " " +
                "ORDER BY A.sortorder";

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

    private void loadLinkedIndicators(Map<Integer, Activity> activityMap, Set<Integer> activityIds) throws SQLException {

        String sql = "SELECT " +
                        "k.sourceIndicatorId, " +
                        "si.activityId, " +
                        "sa.reportingFrequency, " + 
                        "k.destinationIndicatorId, " +
                        "di.activityId " +
                     "FROM indicatorlink k " +
                     "INNER JOIN indicator si ON (si.indicatorId = k.sourceIndicatorId) " +
                     "INNER JOIN activity sa ON (sa.activityid = si.activityid) " + 
                     "INNER JOIN indicator di ON (di.indicatorId = k.destinationIndicatorId) " +
                     "WHERE di.activityId IN " + idList(activityIds);


        try(ResultSet rs = executor.query(sql)) {
            while(rs.next()) {
                int sourceIndicatorId = rs.getInt(1);
                int sourceActivityId =  rs.getInt(2);
                int sourceReportingFrequency = rs.getInt(3);
                int destinationIndicatorId = rs.getInt(4);
                int destinationActivityId = rs.getInt(5);

                Activity destinationActivity = activityMap.get(destinationActivityId);
                destinationActivity.addLink(destinationIndicatorId, sourceActivityId, sourceReportingFrequency, sourceIndicatorId);
            }   
        }
    }

}
