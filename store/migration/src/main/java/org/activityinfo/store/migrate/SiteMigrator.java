package org.activityinfo.store.migrate;

import com.google.appengine.api.datastore.Text;
import com.google.appengine.tools.mapreduce.MapOnlyMapper;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.googlecode.objectify.VoidWork;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonException;
import org.activityinfo.model.legacy.CuidAdapter;
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
import org.activityinfo.store.hrd.Hrd;
import org.activityinfo.store.hrd.entity.FormRecordEntity;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.metadata.Activity;
import org.activityinfo.store.mysql.metadata.ActivityField;
import org.activityinfo.store.mysql.metadata.ActivityLoader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

public class SiteMigrator extends MapOnlyMapper<Integer, Void> {

    private static final Logger LOGGER = Logger.getLogger(SiteMigrator.class.getName());

    private transient QueryExecutor queryExecutor;
    private transient ActivityLoader activityLoader;


    @Override
    public void beginSlice() {
        super.beginSlice();
        try {
            queryExecutor = new MySqlQueryExecutor();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        activityLoader = new ActivityLoader(queryExecutor);

    }

    @Override
    public void map(Integer siteId) {

        try (ResultSet site = queryExecutor.query("select s.activityId, s.partnerId, s.projectId, " +
                "s.date1, s.date2, s.comments " +
                "from site s " +
                "where siteid = ?", siteId)) {

            if(!site.next()) {
                throw new RuntimeException("No such site " + siteId);
            }

            int activityId = site.getInt("activityId");

            Activity activity = activityLoader.load(activityId);
            int databaseId = activity.getDatabaseId();
            ResourceId formId = activity.getSiteFormClassId();
            ResourceId recordId = CuidAdapter.resourceId(CuidAdapter.SITE_DOMAIN, siteId);


            Map<ResourceId, FieldValue> fieldValues = new HashMap<>();

            Date date1 = site.getDate("date1");
            if(!site.wasNull()) {
                fieldValues.put(CuidAdapter.field(formId, CuidAdapter.START_DATE_FIELD), new LocalDate(date1));
            }

            Date date2 = site.getDate("date2");
            if(!site.wasNull()) {
                fieldValues.put(CuidAdapter.field(formId, CuidAdapter.END_DATE_FIELD), new LocalDate(date2));
            }

            int partnerId = site.getInt("partnerId");
            fieldValues.put(CuidAdapter.partnerField(activityId), CuidAdapter.partnerRef(databaseId, partnerId));

            int projectId = site.getInt("projectId");
            if (!site.wasNull()) {
                fieldValues.put(CuidAdapter.projectField(activityId), CuidAdapter.projectRef(databaseId, projectId));
            }

            String comments = site.getString("comments");
            if(!Strings.isNullOrEmpty(comments)){
                fieldValues.put(CuidAdapter.field(formId, CuidAdapter.COMMENT_FIELD), TextValue.valueOf(comments));
            }

            queryIndicatorValues(activity, siteId, fieldValues);
            queryAttributes(activity, siteId, fieldValues);

            FormRecordEntity recordEntity = new FormRecordEntity(activity.getSiteFormClassId(), recordId);
            recordEntity.setVersion(1);
            recordEntity.setSchemaVersion(0);
            recordEntity.setFieldValues(activity.getSerializedFormClass(), fieldValues);

            maybeUpdate(recordEntity);


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void maybeUpdate(FormRecordEntity recordEntity) {
        Hrd.run(new VoidWork() {
            @Override
            public void vrun() {

                Key<FormRecordEntity> recordKey = recordEntity.getKey();
                LoadResult<FormRecordEntity> existing = Hrd.ofy().load().key(recordKey);
                if(existing.now() == null) {
                    LOGGER.info("Found missing FormRecord entity: " + recordKey + stringify(recordEntity));

                    getContext().getCounter("missing").increment(1);

                } else {
                    if(!fieldsIdentical(recordEntity, existing.now())) {
                        getContext().getCounter("inconsistent").increment(1);
                    } else {
                        getContext().getCounter("valid").increment(1);
                    }
                }

            }
        });
    }

    private boolean fieldsIdentical(FormRecordEntity mysql, FormRecordEntity hrd) {
        Map<String, Object> mysqlProps = mysql.getFieldValues().getProperties();
        Map<String, Object> hrdProps = hrd.getFieldValues().getProperties();

        StringBuilder diff = new StringBuilder();
        boolean identical = true;

        for (String field : mysqlProps.keySet()) {

            Object mysqlValue = mysqlProps.get(field);
            Object hrdValue = hrdProps.get(field);

            if(!fieldsIdentical(mysqlValue, hrdValue)) {
                diff.append("\nField " + field + " has unequal values: MySQL = " + mysqlValue + ", HRD = " + hrdValue);
                identical = false;
            }
        }

        for (String field : hrdProps.keySet()) {
            Object hrdValue = hrdProps.get(field);

            if(!mysqlProps.containsKey(field)) {
                diff.append("\nField " + field + " has unequal values: MySQL = null, HRD = " + hrdValue);
                identical = false;
            }
        }

        if(!identical) {
            LOGGER.warning("Site " + mysql.getRecordId() + " in " + mysql.getFormId() + diff.toString());
        }

        return identical;
    }

    private boolean fieldsIdentical(Object mysqlValue, Object hrdValue) {
        if(mysqlValue instanceof Text) {
            mysqlValue = ((Text) mysqlValue).getValue().trim();
        }
        if(hrdValue instanceof Text) {
            hrdValue = ((Text) hrdValue).getValue().trim();
        }
        if(mysqlValue instanceof List && hrdValue instanceof List) {
            return setsEquivalent(((List) mysqlValue), ((List) hrdValue));
        }
        return Objects.equals(mysqlValue, hrdValue);
    }

    private boolean setsEquivalent(List<Object> mysqlList, List<Object> hrdList) {
        return mysqlList.containsAll(hrdList) && hrdList.containsAll(mysqlList);
    }


    private static String stringify(FormRecordEntity recordEntity) {
        StringBuilder s = new StringBuilder();
        s.append("formId = ").append(recordEntity.getFormId().asString());
        s.append("\nrecordId = ").append(recordEntity.getFormId().asString());
        s.append("\nversion = " ).append(recordEntity.getVersion());
        Map<String, Object> fieldValues = recordEntity.getFieldValues().getProperties();
        for (String field : fieldValues.keySet()) {
            s.append("\n").append(field).append(": ").append(fieldValues.get(field));
        }
        return s.toString();
    }


    private void queryIndicatorValues(Activity activity, Integer siteId, Map<ResourceId, FieldValue> fieldValues) {
        try(ResultSet rs = queryExecutor.query("select v.indicatorid, v.value, v.textValue, i.type from indicatorvalue v " +
                "left join indicator i on (v.indicatorId = i.indicatorId) " +
                "left join reportingperiod rp on (rp.reportingPeriodId = v.reportingPeriodId) " +
                "where rp.siteId = ?", siteId)) {

            while(rs.next()) {

                int indicatorId = rs.getInt("indicatorId");
                ResourceId fieldId = CuidAdapter.indicatorField(indicatorId);
                String typeId = rs.getString("type");
                FieldTypeClass typeClass;
                if(rs.wasNull()) {
                    typeClass = findTypeFromSchema(activity, fieldId);
                } else {
                    typeClass = TypeRegistry.get().getTypeClass(typeId);
                }

                if(typeClass != null) {
                    FieldValue fieldValue;
                    if (typeClass == QuantityType.TYPE_CLASS) {
                        fieldValue = new Quantity(rs.getDouble("value"));
                        if (rs.wasNull()) {
                            fieldValue = null;
                        }
                    } else {
                        String textValue = rs.getString("TextValue");
                        if (rs.wasNull()) {
                            fieldValue = null;
                        } else if (typeClass == TextType.TYPE_CLASS) {
                            fieldValue = TextValue.valueOf(textValue);

                        } else if (typeClass == NarrativeType.TYPE_CLASS) {
                            fieldValue = NarrativeValue.valueOf(textValue);

                        } else if (typeClass == BarcodeType.TYPE_CLASS) {
                            fieldValue = BarcodeValue.valueOf(textValue);

                        } else {
                            try {
                                fieldValue = typeClass.createType().parseJsonValue(Json.parse(textValue));
                            } catch (JsonException e) {
                                LOGGER.severe("Failed to parse indicator " + indicatorId + " with type " + typeClass +
                                    " (typeId = '" + typeId + "')");
                                throw e;
                            }
                        }
                    }
                    fieldValues.put(fieldId, fieldValue);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private FieldTypeClass findTypeFromSchema(Activity activity, ResourceId fieldId) {
        for (ActivityField activityField : activity.getFields()) {
            if(activityField.getResourceId().equals(fieldId)) {
                return activityField.getFormField().getType().getTypeClass();
            }
        }
        return null;
    }

    private void queryAttributes(Activity activity, Integer siteId, Map<ResourceId, FieldValue> fieldValues) {
        try(ResultSet rs = queryExecutor.query("select a.AttributeGroupId, av.AttributeId from attributevalue av " +
                "left join attribute a on (av.attributeId = a.attributeId) " +
                "where av.siteId=? and av.value=1", siteId)) {

            Multimap<ResourceId, ResourceId> attributeValues = HashMultimap.create();

            while(rs.next()) {
                int attributeId = rs.getInt("AttributeId");
                ResourceId itemId = CuidAdapter.attributeId(attributeId);

                ResourceId fieldId;
                int groupId = rs.getInt("AttributeGroupId");
                if(rs.wasNull()) {
                    fieldId = findGroupIdFromSchema(activity, itemId);
                } else {
                    fieldId = CuidAdapter.attributeGroupField(groupId);
                }
                if(fieldId != null) {
                    attributeValues.put(fieldId, itemId);
                }
            }

            for (ResourceId fieldId : attributeValues.keySet()) {
                fieldValues.put(fieldId, new EnumValue(attributeValues.get(fieldId)));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ResourceId findGroupIdFromSchema(Activity activity, ResourceId itemId) {
        for (ActivityField activityField : activity.getFields()) {
            if(activityField.isAttributeGroup()) {
                EnumType enumType = (EnumType) activityField.getFormField().getType();
                for (EnumItem enumItem : enumType.getValues()) {
                    if (enumItem.getId().equals(itemId)) {
                        return activityField.getResourceId();
                    }
                }
            }
        }

        LOGGER.severe("Could not find group for attribute id " + itemId);
        return null;
    }

}