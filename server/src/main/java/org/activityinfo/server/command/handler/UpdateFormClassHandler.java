package org.activityinfo.server.command.handler;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.legacy.shared.command.UpdateFormClass;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.NarrativeType;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.subform.SubFormType;
import org.activityinfo.server.command.handler.json.JsonHelper;
import org.activityinfo.server.database.hibernate.entity.*;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.activityinfo.model.legacy.CuidAdapter.ACTIVITY_DOMAIN;

public class UpdateFormClassHandler implements CommandHandler<UpdateFormClass> {

    private static final Logger LOGGER = Logger.getLogger(UpdateFormClassHandler.class.getName());

    private final PermissionOracle permissionOracle;
    private final Provider<EntityManager> entityManager;

    @Inject
    public UpdateFormClassHandler(Provider<EntityManager> entityManager, PermissionOracle permissionOracle) {
        this.entityManager = entityManager;
        this.permissionOracle = permissionOracle;
    }

    @Override
    public CommandResult execute(UpdateFormClass cmd, User user) throws CommandException {
        char domain = ResourceId.valueOf(cmd.getFormClassId()).getDomain();
        FormClass formClass = validateFormClass(cmd.getJson());

        if (domain == ACTIVITY_DOMAIN) {
            updateActivityFormClass(cmd, user, formClass);
        } else {

            FormClassEntity hibernateFormClass = new FormClassEntity();

            hibernateFormClass.setId(formClass.getId().asString());
            hibernateFormClass.setOwnerId(formClass.getOwnerId().asString());
            JsonHelper.updateWithJson(hibernateFormClass, cmd.getJson());

            if (!exists(hibernateFormClass.getId())) {
                entityManager.get().persist(hibernateFormClass);
            } else {
                entityManager.get().merge(hibernateFormClass);
            }
        }

        return new VoidResult();
    }

    private boolean exists(String formClassId) {
        return entityManager.get().find(FormClassEntity.class, formClassId) != null;
    }

    private void updateActivityFormClass(UpdateFormClass cmd, User user, FormClass formClass) {
        int activityId = CuidAdapter.getLegacyIdFromCuid(cmd.getFormClassId());
        Activity activity = entityManager.get().find(Activity.class, activityId);

        permissionOracle.assertDesignPrivileges(activity.getDatabase(), user);

        // Update the activity table with the JSON value
        JsonHelper.updateWithJson(activity, cmd.getJson());

        // we should not set it instead of user (looks very weird for end user if mode is changed because of some backend function)
//        activity.setClassicView(false);

        if (cmd.isSyncActivityEntities()) {
            syncEntities(activity, formClass);
        } else {
            entityManager.get().persist(activity);
        }
    }

    private FormClass validateFormClass(String json) {
        try {
            Resource resource = Resources.fromJson(json);
            return FormClass.fromResource(resource);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Invalid FormClass json: " + e.getMessage(), e);
            throw new CommandException();
        }
    }

    /**
     * Synchronize this FormClass representation with the legacy indicators and attributes
     * format. We need to maintain a dual-write layer until the transition from indicators and
     * attributes is complete.
     *
     */
    private void syncEntities(Activity activity, FormClass formClass) {

        activity.setName(formClass.getLabel());

        List<FormFieldEntity> fields = new ArrayList<>();
        fields.addAll(activity.getIndicators());
        fields.addAll(activity.getAttributeGroups());

        Map<ResourceId, FormFieldEntity> entityMap = Maps.newHashMap();
        for(FormFieldEntity field : fields) {
            entityMap.put(field.getFieldId(), field);
        }

        Set<ResourceId> builtinFields = Sets.newHashSet();
        for(int fieldIndex : CuidAdapter.BUILTIN_FIELDS) {
            builtinFields.add(CuidAdapter.field(formClass.getId(), fieldIndex));
        }

        int sortOrder = 1;
        for(FormField field : formClass.getFields()) {
            if(!builtinFields.contains(field.getId())) {
                FormFieldEntity fieldEntity = entityMap.get(field.getId());
                if (fieldEntity == null) {
                    createNewEntity(activity, field, sortOrder);

                } else {
                    updateEntity(fieldEntity, field, sortOrder);
                    entityMap.remove(field.getId());
                }
                sortOrder++;
            }
        }

        // delete any entities that were not matched to FormFields
        for(FormFieldEntity entity : entityMap.values()) {
            entity.delete();
        }
    }

    private void createNewEntity(Activity activity, FormField field, int sortOrder) {
        if(field.getType() instanceof EnumType) {
            createAttributeGroup(activity, field, sortOrder);
        } else {
            createIndicator(activity, field, sortOrder);
        }
    }

    private void updateEntity(FormFieldEntity fieldEntity, FormField field, int sortOrder) {
        if(fieldEntity instanceof AttributeGroup) {
            updateAttributeGroup((AttributeGroup) fieldEntity, field, sortOrder);
        } else {
            updateIndicator((Indicator)fieldEntity, field, sortOrder);
        }
    }

    private void createIndicator(Activity activity, FormField field, int sortOrder) {
        Indicator indicator = new Indicator();
        indicator.setId(CuidAdapter.getLegacyIdFromCuid(field.getId()));
        indicator.setActivity(activity);
        updateIndicatorProperties(indicator, field, sortOrder);

        entityManager.get().persist(indicator);
    }

    private void updateIndicator(Indicator indicator, FormField field, int sortOrder) {
        updateIndicatorProperties(indicator, field, sortOrder);
    }

    private void updateIndicatorProperties(Indicator indicator, FormField field, int sortOrder) {
        indicator.setName(truncate(field.getLabel(), 255));
        indicator.setMandatory(field.isRequired());
        indicator.setDescription(field.getDescription());
        indicator.setSortOrder(sortOrder);
        indicator.setNameInExpression(field.getCode());
        indicator.setSkipExpression(field.getRelevanceConditionExpression());
        indicator.setCalculatedAutomatically(field.getType() instanceof CalculatedFieldType);

        if (field.getType() instanceof QuantityType) {
            indicator.setType(QuantityType.TYPE_CLASS.getId());
            indicator.setUnits(((QuantityType) field.getType()).getUnits());

        } else if(field.getType() instanceof NarrativeType) {
            indicator.setType(NarrativeType.TYPE_CLASS.getId());

        } else if (field.getType() instanceof BooleanType) {
            indicator.setType(BooleanType.TYPE_CLASS.getId());

        } else if (field.getType() instanceof CalculatedFieldType) {
            CalculatedFieldType type = (CalculatedFieldType) field.getType();
            indicator.setType(QuantityType.TYPE_CLASS.getId());
            indicator.setExpression(type.getExpression().getExpression());

        } else if (field.getType() instanceof BarcodeType) {
            indicator.setType(TextType.TYPE_CLASS.getId());

        } else if (field.getType() instanceof SubFormType) {
            SubFormType subFormType = (SubFormType) field.getType();

            indicator.setType(SubFormType.TYPE_CLASS.getId());
            indicator.setTypeJson(Resources.toJsonObject(subFormType.getParameters()).toString());

        } else {
            indicator.setType(field.getType().getTypeClass().getId());
        }
    }

    private String truncate(String label, int maxLength) {
        if(label.length() > maxLength) {
            return label.substring(0, maxLength);
        } else {
            return label;
        }
    }

    private FormFieldEntity createAttributeGroup(Activity activity, FormField field, int sortOrder) {
        EnumType type = (EnumType) field.getType();

        AttributeGroup group = new AttributeGroup();
        group.setId(CuidAdapter.getLegacyIdFromCuid(field.getId()));
        updateAttributeGroupProperties(group, field, sortOrder);

        entityManager.get().persist(group);
        activity.getAttributeGroups().add(group);

        updateAttributes(group, type);

        return group;
    }

    private void updateAttributeGroup(AttributeGroup group, FormField field, int sortOrder) {
        updateAttributeGroupProperties(group, field, sortOrder);
        updateAttributes(group, (EnumType) field.getType());
    }

    private void updateAttributeGroupProperties(AttributeGroup group, FormField field, int sortOrder) {
        group.setName(truncate(field.getLabel(), 255));
        group.setMandatory(field.isRequired());
        group.setMultipleAllowed(((EnumType) field.getType()).getCardinality() == Cardinality.MULTIPLE);
        group.setSortOrder(sortOrder);
    }

    private void updateAttributes(AttributeGroup group, EnumType type) {
        Map<ResourceId, Attribute> attributeMap = new HashMap<>();
        for(Attribute attribute : group.getAttributes()) {
            attributeMap.put(attribute.getResourceId(), attribute);
        }

        // add/update present attributes
        int sortOrder = 1;
        for(EnumItem item : type.getValues()) {
            Attribute attribute = attributeMap.get(item.getId());
            if(attribute == null) {
                attribute = new Attribute();
                attribute.setGroup(group);
                attribute.setId(CuidAdapter.getLegacyIdFromCuid(item.getId()));
                attribute.setName(truncate(item.getLabel(), 255));
                attribute.setSortOrder(sortOrder);
                entityManager.get().persist(attribute);
                group.getAttributes().add(attribute);
            } else {
                // update properties
                attribute.setName(item.getLabel());
                attribute.setSortOrder(sortOrder);
            }
            sortOrder++;
        }

        // remove deleted
        Set<ResourceId> deleted = Sets.newHashSet(attributeMap.keySet());
        for(EnumItem item : type.getValues()) {
            deleted.remove(item.getId());
        }
        for (ResourceId deletedAttribute : deleted) {
            attributeMap.get(deletedAttribute).delete();
        }
    }
}
