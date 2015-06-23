package org.activityinfo.store.query.impl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceUpdate;
import org.activityinfo.model.type.*;
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
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 * Validates and authorizes an update received from the client and
 * passes it on the correct collection
 */
public class Updater {
    
    private static final Logger LOGGER = Logger.getLogger(Updater.class.getName());
    
    private final CollectionCatalog catalog;

    public Updater(CollectionCatalog catalog) {
        this.catalog = catalog;
    }


    /**
     * Validates and executes a {@code ResourceUpdate} encoded as a json object. The object
     * must have a changes property that takes the value of an array. 
     * 
     * @throws org.activityinfo.store.query.impl.InvalidUpdateException if the given update
     * is not a validate update.
     */
    public void execute(JsonObject updateObject) {
        if(!updateObject.has("changes")) {
            throw new InvalidUpdateException("Root object must contain 'changes' property.");
        }
        JsonElement changes = updateObject.get("changes");
        if(!changes.isJsonArray()) {
            throw new InvalidUpdateException("Root object property 'changes' must be an array. " +
                    "Found: " + changes.toString());
        }

        for (JsonElement change : changes.getAsJsonArray()) {
            if(!change.isJsonObject()) {
                throw new InvalidUpdateException("Expected 'changes' property to be an array of json objects, " +
                        "but 'changes' array includes an element: " + change);
            }
            executeChange(change.getAsJsonObject());
        }
    }
    
    public void executeChange(JsonObject changeObject) {
        
        ResourceId resourceId = parseId(changeObject, "@id");

        // First determine whether the resource already exists. 
        Optional<ResourceCollection> collection = catalog.lookupCollection(resourceId);
        
        if(!collection.isPresent()) {
            // If the resource is not present, then we need the @class attribute in order to 
            // know where to put the new resource 
            if(!changeObject.has("@class")) {
                throw new InvalidUpdateException(format(
                    "Resource with id '%s' does not exist and no '@class' attribute has been provided.", resourceId));
            } 
            
            ResourceId collectionId = parseId(changeObject, "@class");
            collection = catalog.getCollection(collectionId);
            
            if(!collection.isPresent()) {
                throw new InvalidUpdateException(format("@class '%s' does not exist.", collectionId));
            }
        }
        

        FormClass formClass = collection.get().getFormClass();
        ResourceUpdate update = parseChange(formClass, changeObject);


        executeUpdate(collection.get(), update);
    }

    @VisibleForTesting
    static ResourceUpdate parseChange(FormClass formClass, JsonObject changeObject) {
        
        // This resource exists, make sure the existing class matches the provided @class
        // if given explicitly
        validateExplicitClassAttribute(formClass, changeObject);


        // Build a lookup map to resolve the field name used in the JSON request.
        Map<String, FormField> idMap = new HashMap<>();
        Multimap<String, FormField> codeMap = HashMultimap.create();

        for (FormField formField : formClass.getFields()) {
            idMap.put(formField.getId().asString(), formField);
            codeMap.put(formField.getCode(), formField);
        }

        ResourceUpdate update = new ResourceUpdate();
        update.setResourceId(parseId(changeObject, "@id"));
        update.setDeleted(parseDeletedFlag(changeObject));

        for(Map.Entry<String, JsonElement> change : changeObject.entrySet()) {
            if(!change.getKey().startsWith("@")) {
                String fieldName = change.getKey();

                // Resolve what might be a human readable field name to the FormField
                // We accept FormField codes and ids
                FormField field;
                if(idMap.containsKey(fieldName)) {
                    field = idMap.get(fieldName);
                } else {
                    Collection<FormField> byCode = codeMap.get(fieldName);
                    if(byCode.size() == 0) {
                        throw new InvalidUpdateException("Unknown field '" + fieldName + "'.");
                    } else if(byCode.size() > 1) {
                        throw new InvalidUpdateException("Ambiguous field code '" + fieldName + "'");
                    }
                    field = Iterables.getOnlyElement(byCode);
                }
                
                // Now use the type information to parse the JSON element
                FieldValue fieldValue;
                try {
                    fieldValue = parseFieldValue(field, changeObject.get(fieldName));
                } catch (InvalidUpdateException e) {
                    throw new InvalidUpdateException(format("Invalid value for field '%s' (id: %s, code: %s): %s",
                            field.getLabel(),
                            field.getId(),
                            field.getCode(),
                            e.getMessage()), e);
                }
                validate(field, fieldValue);
                update.set(field.getId(), fieldValue);
            }
        }
        return update;
    }

    private static boolean parseDeletedFlag(JsonObject changeObject) {
        if(changeObject.has("@deleted")) {
            JsonElement jsonElement = changeObject.get("@deleted");
            if(!jsonElement.isJsonPrimitive()) {
                throw new InvalidUpdateException("The '@deleted' property must be a boolean.");
            }
            return jsonElement.getAsBoolean();
        } else {
            return false;
        }
    }

    private static void validateExplicitClassAttribute(FormClass existingFormClass, JsonObject changeObject) {
        if(changeObject.has("@class")) {
            ResourceId classId = parseId(changeObject, "@class");
            if(!classId.equals(existingFormClass.getId())) {
                throw new InvalidUpdateException("Resource '%s' already exists but is a member of the class " +
                        "'%s' (%s). Cannot change to class %s.", 
                        changeObject.get("@id").getAsString(),
                        existingFormClass.getLabel(),
                        existingFormClass.getId(),
                        classId);
            }
        }
    }

    private static ResourceId parseId(JsonObject changeObject, String propertyName) {
        if(!changeObject.has(propertyName)) {
            throw new InvalidUpdateException(format("Missing '%s' property", propertyName));
        }
        JsonElement jsonValue = changeObject.get(propertyName);
        if(!jsonValue.isJsonPrimitive() || !jsonValue.getAsJsonPrimitive().isString()) {
            throw new InvalidUpdateException(format("Property '%s' must contain a string, but found: %s", 
                    propertyName, jsonValue.toString()));
        }
        
        return ResourceId.valueOf(jsonValue.getAsString());
    }

    private static FieldValue parseFieldValue(FormField field, JsonElement jsonValue) {
        if(jsonValue.isJsonNull()) {
            return null;
        } else if(field.getType() instanceof TextType) {
            return TextValue.valueOf(jsonValue.getAsString());
            
        } else if(field.getType() instanceof QuantityType) {
            return parseQuantity(field, jsonValue);
            
        } else if(field.getType() instanceof EnumType) {
            return parseEnumValue((EnumType) field.getType(), jsonValue.getAsString());
        
        } else if(field.getType() instanceof ReferenceType) {
            return new ReferenceValue(ResourceId.valueOf(jsonValue.getAsString()));    
        
        } else if(field.getType() instanceof LocalDateType) {
            return parseDate(jsonValue.getAsString());    
            
        } else if(field.getType() instanceof NarrativeType) {
            return NarrativeValue.valueOf(jsonValue.getAsString());
            
        }
        throw new InvalidUpdateException("Unsupported type: " + field.getType().getTypeClass().getId());
    }

    private static FieldValue parseQuantity(FormField field, JsonElement jsonValue) {
        QuantityType quantityType = (QuantityType) field.getType();

        if(!jsonValue.isJsonPrimitive()) {
            throw new InvalidUpdateException("Quantity fields must be encoded as JSON number, found: %s", jsonValue);
        }
        if(jsonValue.getAsJsonPrimitive().isNumber()) {
            return new Quantity(jsonValue.getAsDouble(), quantityType.getUnits());
            
        } else if(jsonValue.getAsJsonPrimitive().isString()) {
            try {
                return new Quantity(Double.parseDouble(jsonValue.getAsString()), quantityType.getUnits());
            } catch (NumberFormatException e) {
                throw new InvalidUpdateException("Invalid number: " + e.getMessage(), e);
            }
            
        } else {
            throw new InvalidUpdateException("Expected number, found: " + jsonValue);
        }
    }

    private static FieldValue parseDate(String jsonValue) {
        return org.activityinfo.model.type.time.LocalDate.parse(jsonValue);
    }

    private static FieldValue parseEnumValue(EnumType type, String jsonValue) {
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
        
        throw new InvalidUpdateException(format("Invalid enum value '%s', expected one of: %s", 
                jsonValue, Joiner.on(", ").join(type.getValues())));
    }

    public void execute(ResourceUpdate update) {
        Optional<ResourceCollection> collection = catalog.lookupCollection(update.getResourceId());
        if(!collection.isPresent()) {
            throw new InvalidUpdateException("No such resource: " + update.getResourceId());
        }

        executeUpdate(collection.get(), update);
    }

    private void executeUpdate(ResourceCollection collection, ResourceUpdate update) {
        
        FormClass formClass = collection.getFormClass();
        Optional<Resource> existingResource = collection.get(update.getResourceId());

        LOGGER.info("Loaded existingResource " + existingResource);

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

    private static void validate(FormField field, FieldValue updatedValue) {
        Preconditions.checkNotNull(field);
        
        if(updatedValue == null) {
            if(field.isRequired()) {
                throw new InvalidUpdateException(
                        format("Field '%s' (id: %s, code: %s) is required. Found 'null'",
                                field.getLabel(),
                                field.getId(), field.getCode()));
            }
        } else {
            if (!field.getType().getTypeClass().equals(updatedValue.getTypeClass())) {
                throw new InvalidUpdateException(
                        format("Updated value for field %s ('%s') has invalid type. Expected %s, found %s.",
                                field.getId(),
                                field.getLabel(),
                                field.getType().getTypeClass().getId(),
                                updatedValue.getTypeClass().getId()));
            }
        }
        // TODO: check type-class specific properties
    }

}
