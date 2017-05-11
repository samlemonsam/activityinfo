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
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.ExprParser;
import org.activityinfo.model.form.*;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.SerialNumber;
import org.activityinfo.model.type.SerialNumberType;
import org.activityinfo.model.type.attachment.Attachment;
import org.activityinfo.model.type.attachment.AttachmentType;
import org.activityinfo.model.type.attachment.AttachmentValue;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.store.spi.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 * Validates and authorizes an update received from the client and
 * passes it on the correct form accessor.
 *
 * <p>This class enforces all logical permissions related to the form. </p>
 */
public class Updater {
    
    private static final Logger LOGGER = Logger.getLogger(Updater.class.getName());
    
    private final FormCatalog catalog;
    private int userId;
    private BlobAuthorizer blobAuthorizer;
    private SerialNumberProvider serialNumberProvider;

    private boolean enforcePermissions = true;

    public Updater(FormCatalog catalog, int userId,
                   BlobAuthorizer blobAuthorizer,
                   SerialNumberProvider serialNumberProvider) {
        this.catalog = catalog;
        this.userId = userId;
        this.blobAuthorizer = blobAuthorizer;
        this.serialNumberProvider = serialNumberProvider;
    }

    public void setEnforcePermissions(boolean enforcePermissions) {
        this.enforcePermissions = enforcePermissions;
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
        
        ResourceId recordId = parseId(changeObject, "@id");

        if(!changeObject.has("@class")) {
            throw new InvalidUpdateException(format(
                "Resource with id '%s' does not exist and no '@class' attribute has been provided.", recordId));
        }

        ResourceId formId = parseId(changeObject, "@class");
        Optional<FormStorage> storage = catalog.getForm(formId);

        if(!storage.isPresent()) {
            throw new InvalidUpdateException(format("@class '%s' does not exist.", formId));
        }

        FormClass formClass = storage.get().getFormClass();
        RecordUpdate update = parseChange(formClass, changeObject, this.userId);

        executeUpdate(storage.get(), update);
    }

    @VisibleForTesting
    static RecordUpdate parseChange(FormClass formClass, JsonObject changeObject, int userId) {
        
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

        RecordUpdate update = new RecordUpdate();
        update.setUserId(userId);
        update.setRecordId(parseId(changeObject, "@id"));
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
                } catch (Exception e) {
                    throw new InvalidUpdateException(format("Invalid value for field '%s' (id: %s, code: %s): %s",
                            field.getLabel(),
                            field.getId(),
                            field.getCode(),
                            e.getMessage()), e);
                }

                update.set(field.getId(), validateType(field, fieldValue));
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
        } else if(field.getType() instanceof EnumType) {
            return parseEnumValue((EnumType)field.getType(), jsonValue.getAsString());
        } else {
            return field.getType().parseJsonValue(jsonValue);
        }
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


    public void execute(RecordUpdate update) {
        if(update.getFormId() == null) {
            throw new IllegalArgumentException("No formId provided.");
        }
        Optional<FormStorage> storage = catalog.getForm(update.getFormId());
        if(!storage.isPresent()) {
            throw new InvalidUpdateException("No such resource: " + update.getRecordId());
        }

        executeUpdate(storage.get(), update);
    }

    private void executeUpdate(FormStorage form, RecordUpdate update) {
        
        FormClass formClass = form.getFormClass();
        Optional<FormRecord> existingResource = form.get(update.getRecordId());

        if(update.isDeleted() && update.getChangedFieldValues().size() > 0) {
            throw new InvalidUpdateException("A deletion may not include field value updates.");
        }
        if(!update.isDeleted()) {
            validateUpdate(formClass, existingResource, update);
        }
        authorizeUpdate(form, existingResource, update);

        if(existingResource.isPresent()) {
            form.update(update);
        } else {

            generateSerialNumbers(formClass, update);

            form.add(update);
        }
    }

    private void generateSerialNumbers(FormClass formClass, RecordUpdate update) {
        for (FormField formField : formClass.getFields()) {
            if(formField.getType() instanceof SerialNumberType) {
                generateSerialNumber(formClass, formField, update);
            }
        }
    }

    @VisibleForTesting
    void generateSerialNumber(FormClass formClass, FormField formField, RecordUpdate update) {

        SerialNumberType type = (SerialNumberType) formField.getType();
        String prefix = computeSerialNumberPrefix(formClass, type, update);

        int serialNumber = serialNumberProvider.next(formClass.getId(), formField.getId(), prefix);

        update.set(formField.getId(), new SerialNumber(prefix, serialNumber));
    }

    private String computeSerialNumberPrefix(FormClass formClass, SerialNumberType type, RecordUpdate update) {

        if(!type.hasPrefix()) {
            return null;
        }

        try {
            FormInstance record = new FormInstance(update.getRecordId(), formClass.getId());
            for (Map.Entry<ResourceId, FieldValue> entry : update.getChangedFieldValues().entrySet()) {
                record.set(entry.getKey(), entry.getValue());
            }

            FormEvalContext evalContext = new FormEvalContext(formClass);
            evalContext.setInstance(record);

            ExprNode formula = ExprParser.parse(type.getPrefixFormula());
            FieldValue prefixValue = formula.evaluate(evalContext);

            if(prefixValue instanceof TextValue) {
                return ((TextValue) prefixValue).asString();
            } else {
                throw new IllegalStateException("Prefix " + type.getPrefixFormula() + " resolves to type " +
                        prefixValue.getTypeClass().getId());
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to compute prefix for serial number", e);
            return null;
        }
    }


    public static void validateUpdate(FormClass formClass, Optional<FormRecord> existingResource, RecordUpdate update) {
        LOGGER.info("Loaded existingResource " + existingResource);

        Map<ResourceId, FormField> fieldMap = new HashMap<>();
        for (FormField formField : formClass.getFields()) {
            fieldMap.put(formField.getId(), formField);
        }

        // Verify that provided types are correct
        for (Map.Entry<ResourceId, FieldValue> change : update.getChangedFieldValues().entrySet()) {
            FormField field = fieldMap.get(change.getKey());
            if(field == null) {
                throw new InvalidUpdateException("No such field '%s'", change.getKey());
            }
            validateType(field, change.getValue());
        }

        // AI-1578 Allow missing fields
        //validateRequiredFields(formClass, existingResource, update);
    }

    /**
     * Verify that all required fields are provided for new resources
     */
    private static void validateRequiredFields(FormClass formClass, Optional<FormRecord> existingResource, RecordUpdate update) {
        if(!existingResource.isPresent()) {
            for (FormField formField : formClass.getFields()) {
                if (formField.isRequired() &&
                    formField.isVisible() &&
                    formField.getType().isUpdatable() && !isProvided(formField, existingResource, update)) {
                    throw new InvalidUpdateException("Required field '%s' [%s] is missing from record with schema %s",
                            formField.getCode(), formField.getId(), formClass.getId().asString());
                }
            }
        }
    }

    private static boolean isProvided(FormField formField, Optional<FormRecord> existingResource, RecordUpdate update) {

        if(update.getChangedFieldValues().containsKey(formField.getId())) {

            // This update includes an explict update for the given field.
            // The updated value must not be null.

            FieldValue updatedValue = update.getChangedFieldValues().get(formField.getId());
            return updatedValue != null;

        } else {
            // This update does *not* include an updated value for this required field.
            // This is only possible if this is indeed and update and not a new record.

            return existingResource.isPresent();
        }
    }

    private void authorizeUpdate(FormStorage form, Optional<FormRecord> existingResource, RecordUpdate update) {


        // Check form-level permissions
        if(enforcePermissions) {
            FormPermissions permissions = form.getPermissions(userId);
            if (!permissions.isEditAllowed()) {
                throw new InvalidUpdateException("User '%d' does not have edit permissions for form '%s'",
                        userId,
                        form.getFormClass().getId().asString());
            }
        }

        // Check field-level permissions
        FormClass formClass = form.getFormClass();
        for (Map.Entry<ResourceId, FieldValue> change : update.getChangedFieldValues().entrySet()) {
            if(change.getValue() instanceof AttachmentValue) {
                FormField field = formClass.getField(change.getKey());
                checkBlobPermissions(field, existingResource, (AttachmentValue) change.getValue());
            }
        }
    }

    private static FieldValue validateType(FormField field, FieldValue updatedValue) {
        Preconditions.checkNotNull(field);
        
        if(updatedValue != null) {
            if ( !field.getType().isUpdatable()) {
                throw new InvalidUpdateException(
                        format("Field %s ('%s') is a field of type '%s' and its value cannot be set. Found %s",
                                field.getId(),
                                field.getLabel(),
                                field.getType().getTypeClass().getId(),
                                updatedValue));
            }
            if (!field.getType().getTypeClass().equals(updatedValue.getTypeClass())) {
                throw new InvalidUpdateException(
                        format("Updated value for field %s ('%s') has invalid type. Expected %s, found %s.",
                                field.getId(),
                                field.getLabel(),
                                field.getType().getTypeClass().getId(),
                                updatedValue.getTypeClass().getId()));
            }
        }

        return updatedValue;
    }

    /**
     * Verifies that the user has permission to associate the given blob with this record.
     *
     * <p>Updating blob-valued fields is done by the user in two steps. First, the user uploads a file and
     * receives a unique id for the blob. Then, the user updates a record's field with the blob's unique id. </p>
     *
     * <p>Once the blob is associated with the record, then any user with permission to view the record is extended
     * permission to view the blob. This opens an avenue of attack where by an attacker with seeks to obtain access
     * to a blob with some id by assigning it to an unrelated record to which they have access.</p>
     *
     * <p>For this reason, only users who originally uploaded the blob may assign the blob to a record's field value.</p>
     */
    private void checkBlobPermissions(
            FormField field,
            Optional<FormRecord> existingResource,
            AttachmentValue updatedValue) {

        AttachmentType fieldType = (AttachmentType) field.getType();

        // Identity the blob ids that are already associated with this record
        Set<String> existingBlobIds = new HashSet<>();
        if(existingResource.isPresent()) {
            JsonElement existingFieldValue = existingResource.get().getFields().get(field.getId().asString());
            if(existingFieldValue != null) {
                AttachmentValue existingValue = fieldType.parseJsonValue(existingFieldValue);
                for (Attachment attachment : existingValue.getValues()) {
                    existingBlobIds.add(attachment.getBlobId());
                }
            }
        }

        // Assert that the user owns the blob they are associating with the record
        for (Attachment attachment : updatedValue.getValues()) {
            if(!existingBlobIds.contains(attachment.getBlobId())) {
                if(!blobAuthorizer.isOwner(userId, attachment.getBlobId())) {
                    throw new InvalidUpdateException(String.format("User %d does not own blob %s",
                            userId, attachment.getBlobId()));
                }
            }
        }
    }


    public void execute(FormInstance formInstance) {

        Optional<FormStorage> collection = catalog.getForm(formInstance.getFormId());
        if(!collection.isPresent()) {
            throw new InvalidUpdateException("No such formId: " + formInstance.getFormId());
        }

        RecordUpdate update = new RecordUpdate();
        update.setUserId(userId);
        update.setRecordId(formInstance.getId());

        for (Map.Entry<ResourceId, FieldValue> entry : formInstance.getFieldValueMap().entrySet()) {
            if(!entry.getKey().asString().equals("classId")) {
                update.set(entry.getKey(), entry.getValue());
            }
        }
        executeUpdate(collection.get(), update);
    }


    public void create(ResourceId formId, JsonObject jsonObject) {
        String id = jsonObject.get("id").getAsString();
        createOrUpdate(formId, ResourceId.valueOf(id), jsonObject, true);
    }

    public void execute(ResourceId formId, ResourceId recordId, JsonObject jsonObject) {
        createOrUpdate(formId, recordId, jsonObject, false);
    }

    private void createOrUpdate(ResourceId formId, ResourceId recordId, JsonObject jsonObject, boolean create) {
        Optional<FormStorage> collection = catalog.getForm(formId);
        if(!collection.isPresent()) {
            throw new InvalidUpdateException("No such formId: " + formId);
        }

        RecordUpdate update = new RecordUpdate();
        update.setUserId(userId);
        update.setRecordId(recordId);

        if(jsonObject.has("deleted") && !jsonObject.get("deleted").isJsonNull()) {
            update.setDeleted(jsonObject.get("deleted").getAsBoolean());
        }

        if (jsonObject.has("parentRecordId") && !jsonObject.get("parentRecordId").isJsonNull()) {
            update.setParentId(ResourceId.valueOf(jsonObject.get("parentRecordId").getAsString()));
        }

        FormClass formClass = collection.get().getFormClass();
        JsonObject fieldValues = jsonObject.getAsJsonObject("fieldValues");
        for (FormField formField : formClass.getFields()) {
            if(!(formField.getType() instanceof CalculatedFieldType)) {
                if (fieldValues.has(formField.getName())) {
                    JsonElement updatedValueElement = fieldValues.get(formField.getName());
                    FieldValue updatedValue;
                    if(updatedValueElement.isJsonNull()) {
                        updatedValue = null;
                    } else {
                        try {
                            updatedValue = formField.getType().parseJsonValue(updatedValueElement);
                        } catch(Exception e) {
                            throw new InvalidUpdateException("Could not parse updated value for field " +
                                formField.getId() + ": " + e.getMessage());
                        }
                    }
                    update.getChangedFieldValues().put(formField.getId(), updatedValue);

                } else if (create) {
                    update.getChangedFieldValues().put(formField.getId(), null);
                }
            }
        }

        executeUpdate(collection.get(), update);
    }
}
