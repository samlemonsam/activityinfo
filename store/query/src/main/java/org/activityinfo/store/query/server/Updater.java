package org.activityinfo.store.query.server;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonMappingException;
import org.activityinfo.json.JsonObject;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.ExprParser;
import org.activityinfo.model.form.*;
import org.activityinfo.model.resource.RecordTransaction;
import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.SerialNumber;
import org.activityinfo.model.type.SerialNumberType;
import org.activityinfo.model.type.attachment.Attachment;
import org.activityinfo.model.type.attachment.AttachmentType;
import org.activityinfo.model.type.attachment.AttachmentValue;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.store.spi.*;
import org.omg.CORBA.DynAnyPackage.Invalid;

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
     * @throws InvalidUpdateException if the given update
     * is not a validate update.
     */
    public void execute(JsonObject transactionObject) {
        try {
            execute(Json.fromJson(RecordTransaction.class, transactionObject));
        } catch (JsonMappingException e) {
            throw new InvalidUpdateException(e);
        }
    }

    public void execute(RecordTransaction tx) {
        for (RecordUpdate change : tx.getChanges()) {
            executeChange(change);
        }
    }

    public void executeChange(RecordUpdate update) {

        Optional<FormStorage> storage = catalog.getForm(update.getFormId());

        if(!storage.isPresent()) {
            throw new InvalidUpdateException(format("Form '%s' does not exist.", update.getFormId()));
        }

        FormClass formClass = storage.get().getFormClass();
        TypedRecordUpdate typedUpdate = parseChange(formClass, update, this.userId);

        executeUpdate(storage.get(), typedUpdate);
    }

    @VisibleForTesting
    static TypedRecordUpdate parseChange(FormClass formClass, JsonObject changeObject, int userId) throws JsonMappingException {
        return parseChange(formClass, Json.fromJson(RecordUpdate.class, changeObject), userId);
    }

    @VisibleForTesting
    static TypedRecordUpdate parseChange(FormClass formClass, RecordUpdate changeObject, int userId) {

        // Build a lookup map to resolve the field name used in the JSON request.
        Map<String, FormField> idMap = new HashMap<>();
        Multimap<String, FormField> codeMap = HashMultimap.create();

        for (FormField formField : formClass.getFields()) {
            idMap.put(formField.getId().asString(), formField);
            codeMap.put(formField.getCode(), formField);
        }

        TypedRecordUpdate update = new TypedRecordUpdate();
        update.setUserId(userId);
        update.setFormId(formClass.getId());
        update.setRecordId(changeObject.getRecordId());
        update.setDeleted(changeObject.isDeleted());
        if(changeObject.getParentRecordId() != null) {
            update.setParentId(ResourceId.valueOf(changeObject.getParentRecordId()));
        }

        if(changeObject.getFields() != null) {
            for (Map.Entry<String, JsonValue> change : changeObject.getFields().entrySet()) {
                String fieldName = change.getKey();

                // Resolve what might be a human readable field name to the FormField
                // We accept FormField codes and ids
                FormField field;
                if (idMap.containsKey(fieldName)) {
                    field = idMap.get(fieldName);
                } else {
                    Collection<FormField> byCode = codeMap.get(fieldName);
                    if (byCode.size() == 0) {
                        throw new InvalidUpdateException("Unknown field '" + fieldName + "'.");
                    } else if (byCode.size() > 1) {
                        throw new InvalidUpdateException("Ambiguous field code '" + fieldName + "'");
                    }
                    field = Iterables.getOnlyElement(byCode);
                }

                // Now use the type information to parse the JSON element
                FieldValue fieldValue;
                try {
                    fieldValue = parseFieldValue(field, changeObject.getFields().get(fieldName));
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

    private static FieldValue parseFieldValue(FormField field, JsonValue jsonValue) {
        if(jsonValue.isJsonNull()) {
            return null;
        } else if(field.getType() instanceof EnumType) {
            return parseEnumValue((EnumType)field.getType(), jsonValue);
        } else {
            return field.getType().parseJsonValue(jsonValue);
        }
    }

    private static FieldValue parseEnumValue(EnumType type, JsonValue JsonValue) {

        Set<ResourceId> itemIds = new HashSet<>();

        if(JsonValue.isJsonPrimitive()) {
            itemIds.add(parseEnumId(type, JsonValue.asString()));
        } else if(JsonValue.isJsonArray()) {
            for (JsonValue element : JsonValue.getAsJsonArray().values()) {
                itemIds.add(parseEnumId(type, element.asString()));
            }
        }
        if(type.getCardinality() == Cardinality.SINGLE && itemIds.size() > 1) {
            throw new InvalidUpdateException("Field with SINGLE enum type has multiple values.");
        }

        return new EnumValue(itemIds);
    }

    private static ResourceId parseEnumId(EnumType type, String item) {

        for (EnumItem enumItem : type.getValues()) {
            if(enumItem.getId().asString().equals(item)) {
                return enumItem.getId();
            }
        }
        for (EnumItem enumItem : type.getValues()) {
            if(enumItem.getLabel().equals(item)) {
                return enumItem.getId();
            }
        }

        throw new InvalidUpdateException(format("Invalid enum value '%s', expected one of: %s",
            item, Joiner.on(", ").join(type.getValues())));
    }


    public void execute(TypedRecordUpdate update) {
        if(update.getFormId() == null) {
            throw new IllegalArgumentException("No formId provided.");
        }
        Optional<FormStorage> storage = catalog.getForm(update.getFormId());
        if(!storage.isPresent()) {
            throw new InvalidUpdateException("No such resource: " + update.getRecordId());
        }

        executeUpdate(storage.get(), update);
    }

    private void executeUpdate(FormStorage form, TypedRecordUpdate update) {

        Preconditions.checkNotNull(update.getFormId());

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

    private void generateSerialNumbers(FormClass formClass, TypedRecordUpdate update) {
        for (FormField formField : formClass.getFields()) {
            if(formField.getType() instanceof SerialNumberType) {
                generateSerialNumber(formClass, formField, update);
            }
        }
    }

    @VisibleForTesting
    void generateSerialNumber(FormClass formClass, FormField formField, TypedRecordUpdate update) {

        SerialNumberType type = (SerialNumberType) formField.getType();
        String prefix = computeSerialNumberPrefix(formClass, type, update);

        int serialNumber = serialNumberProvider.next(formClass.getId(), formField.getId(), prefix);

        update.set(formField.getId(), new SerialNumber(prefix, serialNumber));
    }

    private String computeSerialNumberPrefix(FormClass formClass, SerialNumberType type, TypedRecordUpdate update) {

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


    public static void validateUpdate(FormClass formClass, Optional<FormRecord> existingResource, TypedRecordUpdate update) {
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
    private static void validateRequiredFields(FormClass formClass, Optional<FormRecord> existingResource, TypedRecordUpdate update) {
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

    private static boolean isProvided(FormField formField, Optional<FormRecord> existingResource, TypedRecordUpdate update) {

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

    private void authorizeUpdate(final FormStorage form, Optional<FormRecord> existingResource, TypedRecordUpdate update) {

        Preconditions.checkNotNull(update.getFormId());

        // Check form-level permissions
        if(enforcePermissions) {

            PermissionsEnforcer enforcer = new PermissionsEnforcer(new FormSupervisorAdapter(catalog, userId), catalog);

            // Verify that the user has the right to modify the *existing* record

            Optional<FormInstance> existingTypedRecord = existingResource.transform(new Function<FormRecord, FormInstance>() {
                @Override
                public FormInstance apply(FormRecord record) {
                    return FormInstance.toFormInstance(form.getFormClass(), record);
                }
            });

            if (existingTypedRecord.isPresent()) {
                if (!enforcer.canEdit(existingTypedRecord.get())) {
                    throw new InvalidUpdateException("Unauthorized update");
                }
            }
            if (!enforcer.canEdit(applyUpdates(existingTypedRecord, update))) {
                throw new InvalidUpdateException("Unauthorized update");
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

    private FormInstance applyUpdates(Optional<FormInstance> existingRecord, TypedRecordUpdate update) {
        FormInstance updated = new FormInstance(update.getRecordId(), update.getFormId());

        if(existingRecord.isPresent()) {
            updated.setAll(existingRecord.get().getFieldValueMap());
        }

        updated.setAll(update.getChangedFieldValues());

        return updated;
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
            JsonValue existingFieldValue = existingResource.get().getFields().get(field.getId().asString());
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

        TypedRecordUpdate update = new TypedRecordUpdate();
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
        String id = jsonObject.get("id").asString();
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

        TypedRecordUpdate update = new TypedRecordUpdate();
        update.setUserId(userId);
        update.setRecordId(recordId);

        if(jsonObject.hasKey("deleted") && !jsonObject.get("deleted").isJsonNull()) {
            update.setDeleted(jsonObject.get("deleted").asBoolean());
        }

        if (jsonObject.hasKey("parentRecordId") && !jsonObject.get("parentRecordId").isJsonNull()) {
            update.setParentId(ResourceId.valueOf(jsonObject.get("parentRecordId").asString()));
        }

        FormClass formClass = collection.get().getFormClass();
        JsonObject fieldValues = jsonObject.get("fieldValues").getAsJsonObject();
        for (FormField formField : formClass.getFields()) {
            if(formField.getType().isUpdatable()) {
                if (fieldValues.hasKey(formField.getName())) {
                    JsonValue updatedValueElement = fieldValues.get(formField.getName());
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
