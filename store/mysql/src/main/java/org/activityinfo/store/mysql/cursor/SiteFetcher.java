package org.activityinfo.store.mysql.cursor;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonException;
import org.activityinfo.model.form.FormInstance;
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
import org.activityinfo.store.mysql.metadata.Activity;
import org.activityinfo.store.mysql.metadata.ActivityField;
import org.activityinfo.store.mysql.metadata.ActivityLoader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class SiteFetcher {

    private static final Logger LOGGER = Logger.getLogger(SiteFetcher.class.getName());

    private final ActivityLoader activityLoader;
    private final QueryExecutor queryExecutor;

    public SiteFetcher(ActivityLoader activityLoader, QueryExecutor queryExecutor) {
        this.activityLoader = activityLoader;
        this.queryExecutor = queryExecutor;
    }

    public Optional<FormInstance> fetch(int siteId) {
        try (ResultSet site = queryExecutor.query("select s.activityId, s.partnerId, s.projectId, " +
                "s.date1, s.date2, s.comments " +
                "from site s " +
                "where siteid = ?", siteId)) {

            if(!site.next()) {
                return Optional.absent();
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

            FormInstance record = new FormInstance(new RecordRef(activity.getSiteFormClassId(), recordId));
            record.setAll(fieldValues);

            return Optional.of(record);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
