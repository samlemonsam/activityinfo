package org.activityinfo.store.query.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceUpdate;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.model.type.time.LocalDateType;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.service.store.ResourceCollection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

/**
 * Validates and authorizes an update received from the client and
 * passes it on the correct collection
 */
public class Updater {
    
    private final CollectionCatalog catalog;

    public Updater(CollectionCatalog catalog) {
        this.catalog = catalog;
    }


    public void execute(JsonObject updateObject) {
        JsonArray changes = updateObject.getAsJsonArray("changes");
        if(changes == null) {
            throw new IllegalArgumentException("Expected 'changes' property on root object");
        }

        for (JsonElement change : changes) {
            executeChange(change.getAsJsonObject());
        }
    }
    
    public void executeChange(JsonObject changeObject) {
        
        ResourceId resourceId = ResourceId.valueOf(changeObject.get("@id").getAsString());

        Optional<ResourceCollection> collection = catalog.lookupCollection(resourceId);
        
        if(!collection.isPresent()) {
            if(changeObject.has("@class")) {
                ResourceId collectionId = ResourceId.valueOf(changeObject.get("@class").getAsString());
                collection = Optional.of(catalog.getCollection(collectionId));
            } else {
                throw new IllegalArgumentException(
                    format("Resource with id '%s' does not exist and no '@class' attribute has been provided.", 
                            resourceId));
            }
        }

        FormClass formClass = collection.get().getFormClass();

        Map<String, FormField> idMap = new HashMap<>();
        Multimap<String, FormField> codeMap = HashMultimap.create();

        for (FormField formField : formClass.getFields()) {
            idMap.put(formField.getId().asString(), formField);
            codeMap.put(formField.getCode(), formField);
        }
        
        ResourceUpdate update = new ResourceUpdate();
        update.setResourceId(resourceId);
        
        for(Map.Entry<String, JsonElement> change : changeObject.entrySet()) {
            if(!change.getKey().startsWith("@")) {
                String fieldName = change.getKey();
                FormField field;
                if(idMap.containsKey(fieldName)) {
                    field = idMap.get(fieldName);
                } else {
                    Collection<FormField> byCode = codeMap.get(fieldName);
                    if(byCode.size() == 0) {
                        throw new IllegalArgumentException("Unknown field '" + fieldName + "'.");
                    } else if(byCode.size() > 1) {
                        throw new IllegalArgumentException("Ambiguous field code '" + fieldName + "'");
                    }
                    field = Iterables.getOnlyElement(byCode);
                }
                FieldValue fieldValue = parseFieldValue(field, changeObject.get(fieldName));
                validate(field, fieldValue);
                
                update.set(field.getId(), fieldValue);
            }
        }
        
        executeUpdate(collection.get(), update);
    }

    private FieldValue parseFieldValue(FormField field, JsonElement jsonValue) {
        if(jsonValue.isJsonNull()) {
            return null;
        } else if(field.getType() instanceof TextType) {
            return TextValue.valueOf(jsonValue.getAsString());
            
        } else if(field.getType() instanceof QuantityType) {
            if(!jsonValue.getAsJsonPrimitive().isNumber()) {
                throw new IllegalArgumentException("Expected number value");
            }
            QuantityType quantityType = (QuantityType) field.getType();
            
            return new Quantity(jsonValue.getAsDouble(), quantityType.getUnits());
            
        } else if(field.getType() instanceof EnumType) {
            return parseEnumValue((EnumType) field.getType(), jsonValue.getAsString());
        
        } else if(field.getType() instanceof ReferenceType) {
            return new ReferenceValue(ResourceId.valueOf(jsonValue.getAsString()));    
        
        } else if(field.getType() instanceof LocalDateType) {
            return parseDate(jsonValue.getAsString());    
        }
        throw new UnsupportedOperationException("Unsupported type: " + field.getType().getTypeClass().getId());
    }

    private FieldValue parseDate(String jsonValue) {
        return org.activityinfo.model.type.time.LocalDate.parse(jsonValue);
    }

    private FieldValue parseEnumValue(EnumType type, String jsonValue) {
        for (EnumItem enumItem : type.getValues()) {
            if(enumItem.getId().asString().equals(jsonValue)) {
                return new EnumValue(enumItem.getId());
            }
        }
        for (EnumItem enumItem : type.getValues()) {
            if(enumItem.getLabel().equals(jsonValue)) {
                return new EnumValue(enumItem.getId());
            }
        }
        
        throw new IllegalArgumentException(format("Invalid enum value '%s', expected one of: %s", 
                jsonValue, Joiner.on(", ").join(type.getValues())));
    }

    public void execute(ResourceUpdate update) {
        Optional<ResourceCollection> collection = catalog.lookupCollection(update.getResourceId());
        if(!collection.isPresent()) {
            throw new IllegalArgumentException("No such resource: " + update.getResourceId());
        }

        executeUpdate(collection.get(), update);
    }

    private void executeUpdate(ResourceCollection collection, ResourceUpdate update) {
        
        FormClass formClass = collection.getFormClass();
        Optional<Resource> existingResource = collection.get(update.getResourceId());

        Map<ResourceId, FormField> fieldMap = new HashMap<>();
        for (FormField formField : formClass.getFields()) {
            fieldMap.put(formField.getId(), formField);
        }

        // Verify that provided types are correct
        Map<ResourceId, FieldValue> valueMap = new HashMap<>();
        for (Map.Entry<ResourceId, FieldValue> change : update.getChangedFieldValues().entrySet()) {
            FormField field = fieldMap.get(change.getKey());
            if(field == null) {
                throw new InvalidUpdateException("No such field '%s'", change.getKey());
            }
            FieldValue updatedValue = change.getValue();
            
            validate(field, updatedValue);
            
            valueMap.put(field.getId(), updatedValue);
        }

        // Verify that all required fields are provided for new resources
        if(!existingResource.isPresent()) {
            for (FormField formField : formClass.getFields()) {
                if (formField.isRequired() && valueMap.get(formField.getId()) == null) {
                    throw new InvalidUpdateException("Required field '%s' [%s] is missing",
                            formField.getCode(), formField.getId());
                }
            }
        }
        
        if(existingResource.isPresent()) {
            collection.update(update);
        } else {
            collection.add(update);
        }
    }

    private void validate(FormField field, FieldValue updatedValue) {
        Preconditions.checkNotNull(field);
        
        if(!field.getType().getTypeClass().equals(updatedValue.getTypeClass())) {
            throw new IllegalArgumentException(
                format("Updated value for field %s ('%s') has invalid type. Expected %s, found %s.",
                        field.getId(),
                        field.getLabel(),
                        field.getType().getTypeClass().getId(),
                        updatedValue.getTypeClass().getId()));
        }
        // TODO: check type-class specific properties
    }

}
