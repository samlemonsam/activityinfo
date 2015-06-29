package org.activityinfo.legacy.shared.adapter;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.shared.command.*;
import org.activityinfo.legacy.shared.command.result.BatchResult;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.NarrativeType;
import org.activityinfo.model.type.TextType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.promise.Promise;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Oh god, why, god, why....
 *
 * Re-translate the update FormClass BACK into the legacy model. We can't yet
 * save it as an actual FormClass because the rest of the app still needs to catch up.
 *
 */
public class FormPersister {

    /**
     * Maintain a mapping, during this browser session, of ids that have been assigned
     * to new Cuids.
     */
    private static Map<ResourceId, Integer> newIds = Maps.newHashMap();

    private Dispatcher dispatcher;
    private FormClass form;
    private ActivityDTO activity;
    private Map<Integer, IndicatorDTO> indicators = Maps.newHashMap();
    private Map<Integer, AttributeGroupDTO> attributeGroups = Maps.newHashMap();
    private List<Command> commands = Lists.newArrayList();
    private Map<CreateEntity, ResourceId> oldIds = Maps.newHashMap();

    private List<FormField> newAttributes = Lists.newArrayList();

    public FormPersister(Dispatcher dispatcher, FormClass form) {
        this.dispatcher = dispatcher;
        this.form = form;
    }

    public Promise<Void> persist() {
        int activityId = CuidAdapter.getLegacyIdFromCuid(form.getId());
        return dispatcher.execute(new GetFormViewModel(activityId)).join(new Function<ActivityDTO, Promise<Void>>() {
            @Nullable
            @Override
            public Promise<Void> apply(@Nullable ActivityDTO input) {
                activity = input;
                return syncActivity().join(new Function<Void, Promise<Void>>() {
                    @Nullable
                    @Override
                    public Promise<Void> apply(@Nullable Void input) {
                        return createAttributesForNewGroups();
                    }

                });
            }
        });
    }

    private void indexFields() {
        for(IndicatorDTO indicator : activity.getIndicators()) {
            indicators.put(indicator.getId(), indicator);
        }
        for(AttributeGroupDTO group : activity.getAttributeGroups()) {
            attributeGroups.put(group.getId(), group);
        }
    }

    private Promise<Void> syncActivity() {
        updateActivity();

        // make a list of indicators / attributes
        indexFields();

        // for each field, find the existing field or create a new one
        int sortOrder = 1;
        for(FormField field : form.getFields()) {

            if(isIndicator(field)) {
                updateOrCreateIndicator(field, sortOrder);
            } else if(isAttributeGroup(field)) {
                updateOrCreateAttributeGroup(field, sortOrder);
            }

            sortOrder++;
        }

        deleteRemoved(indicators);
        deleteRemoved(attributeGroups);

        return executeAndCollectNewIds();
    }

    private void updateActivity() {
        String label = form.getLabel();
        if (!activity.getName().equals(label)) {
            Map<String, Object> changes = new HashMap<>();
            changes.put("name", label);
            commands.add(new UpdateEntity(activity, changes) );
        }
    }

    private void deleteRemoved(Map<Integer, ? extends EntityDTO> entityMap) {
        for(EntityDTO removed : entityMap.values()) {
            commands.add(new Delete(removed));
        }
    }

    private Promise<Void> executeAndCollectNewIds() {
        if(commands.isEmpty()) {
            return Promise.done();
        } else {
            return dispatcher.execute(new BatchCommand(commands)).then(new Function<BatchResult, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable BatchResult input) {
                    mapNewIds(input);
                    commands.clear();
                    return null;
                }
            });
        }
    }


    private boolean isAttributeGroup(FormField field) {
        return field.getType() instanceof EnumType;
    }

    private void mapNewIds(BatchResult results) {
        for(int i=0;i<commands.size();++i) {
            if(commands.get(i) instanceof CreateEntity) {
                CreateEntity creation = (CreateEntity) commands.get(i);
                CreateResult result = results.getResult(i);
                ResourceId oldId = oldIds.get(creation);
                newIds.put(oldId, result.getNewId());
            }
        }
    }

    private void updateOrCreateIndicator(FormField field, int sortOrder) {
        IndicatorDTO indicator = null;
        boolean created = false;
        if(field.getId().getDomain() == CuidAdapter.INDICATOR_DOMAIN) {
            indicator = indicators.get(CuidAdapter.getLegacyIdFromCuid(field.getId()));
        } else if(newIds.containsKey(field.getId())) {
            indicator = indicators.get(newIds.get(field.getId()));
        }
        if(indicator == null) {
            created = true;
            indicator = new IndicatorDTO();
            indicator.setActivityId(activity.getId());
        } else {
            // remove from so we know it is still used - those remaining
            // will be deleted
            indicators.remove(indicator.getId());
        }

        indicator.setName(field.getLabel());
        indicator.setMandatory(field.isRequired());
        indicator.setDescription(field.getDescription());
        indicator.set("sortOrder", sortOrder);
        indicator.setNameInExpression(field.getNameInExpression());
        indicator.setExpression(field.getExpression());
        indicator.setCalculatedAutomatically(field.getCalculateAutomatically());

        if(field.getType() instanceof QuantityType) {
            indicator.setType(FieldTypeClass.QUANTITY);
            indicator.setUnits(((QuantityType) field.getType()).getUnits());

        } else if(field.getType() instanceof NarrativeType) {
            indicator.setType(FieldTypeClass.NARRATIVE);

        } else {
            indicator.setType(FieldTypeClass.FREE_TEXT);
        }

        if(created) {
            CreateEntity create = new CreateEntity(indicator);
            oldIds.put(create, field.getId());
            commands.add(create);
        } else {
            commands.add(new UpdateEntity(indicator, indicator.getProperties()));
        }
    }


    private void updateOrCreateAttributeGroup(FormField field, int sortOrder) {
        AttributeGroupDTO group = null;
        boolean created = false;
        if(field.getId().getDomain() == CuidAdapter.ATTRIBUTE_GROUP_FIELD_DOMAIN) {
            group = attributeGroups.get(CuidAdapter.getLegacyIdFromCuid(field.getId()));
        } else if(newIds.containsKey(field.getId())) {
            group = attributeGroups.get(newIds.get(field.getId()));
        }
        if(group == null) {
            created = true;
            group = new AttributeGroupDTO();
            group.set("activityId", activity.getId());
        } else {
            // mark as used
            attributeGroups.remove(group.getId());
        }

        EnumType enumType = (EnumType) field.getType();

        group.setName(field.getLabel());
        group.setMultipleAllowed(enumType.getCardinality() == Cardinality.MULTIPLE);
        group.setMandatory(field.isRequired());
        group.setSortOrder(sortOrder);

        if(created) {
            CreateEntity create = new CreateEntity(group);
            oldIds.put(create, field.getId());
            commands.add(create);
            newAttributes.add(field);
        } else {
            commands.add(new UpdateEntity(group, group.getProperties()));
            syncAttributes(group, enumType);
        }

    }

    private void syncAttributes(AttributeGroupDTO group, EnumType enumType) {
        Map<Integer, AttributeDTO> attributeMap = Maps.newHashMap();
        int sortOrder = 0;
        for(AttributeDTO attribute : group.getAttributes()) {
            attribute.set("sortOrder", sortOrder);
            attributeMap.put(attribute.getId(), attribute);
            sortOrder++;
        }
        sortOrder = 0;
        for(EnumValue enumValue : enumType.getValues()) {
            AttributeDTO attr = null;
            if(enumValue.getId().getDomain() == CuidAdapter.ATTRIBUTE_DOMAIN) {
                attr = attributeMap.get(CuidAdapter.getLegacyIdFromCuid(enumValue.getId()));
            } else if(newIds.containsKey(enumValue.getId())) {
                attr = attributeMap.get(newIds.get(enumValue.getId()));
            }

            if(attr == null) {
                commands.add(createAttribute(group.getId(), enumValue, sortOrder));

            } else {
                if(!attr.getName().equals(enumValue.getLabel()) ||
                   attr.<Integer>get("sortOrder") != sortOrder) {

                    attr.setName(enumValue.getLabel());
                    attr.set("sortOrder", sortOrder);

                    commands.add(new UpdateEntity(attr, attr.getProperties()));
                }
                attributeMap.remove(attr.getId());
            }

            sortOrder++;
        }

        // Delete all unreferenced attribute groups
        deleteRemoved(attributeMap);

    }

    private CreateEntity createAttribute(int groupId, EnumValue enumValue, int sortOrder) {
        AttributeDTO attr;
        attr = new AttributeDTO();
        attr.setName(enumValue.getLabel());
        attr.set("attributeGroupId", groupId);
        attr.set("sortOrder", sortOrder);
        CreateEntity create = new CreateEntity(attr);
        oldIds.put(create, enumValue.getId());
        return create;
    }

    private boolean isIndicator(FormField field) {

        if(field.getId().equals(CuidAdapter.commentsField(activity.getId()))) {
            return false;
        }

        return field.getType() instanceof QuantityType ||
               field.getType() instanceof NarrativeType ||
               field.getType() instanceof TextType;
    }


    private Promise<Void> createAttributesForNewGroups() {
        for(FormField field : newAttributes) {
            int groupId = newIds.get(field.getId());
            EnumType enumType = (EnumType) field.getType();
            int sortOrder = 0;
            for (EnumValue enumValue : enumType.getValues()) {
                commands.add(createAttribute(groupId, enumValue, sortOrder));
                sortOrder++;
            }
        }
        return executeAndCollectNewIds();
    }

}
