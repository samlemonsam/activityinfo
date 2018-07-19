package org.activityinfo.store.hrd.op;

import com.google.appengine.api.datastore.*;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.store.hrd.columns.*;
import org.activityinfo.store.query.shared.columns.ViewBuilderFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ColumnBlockUpdater {
    private final ResourceId formId;
    private final FormClass formClass;

    /**
     * Map of changed blocks
     */
    private final Map<Key, Entity> blockMap = new HashMap<>();

    private final DatastoreService datastore;
    private Transaction transaction;

    public ColumnBlockUpdater(FormClass formClass, Transaction transaction) {
        this.formId = formClass.getId();
        this.formClass = formClass;
        datastore = DatastoreServiceFactory.getDatastoreService();
        this.transaction = transaction;

    }

    /**
     * Updates the Record ID block to the given record id
     *
     * @param recordNumber one-based record number
     * @param recordId the record Id to set
     */
    public void updateId(int recordNumber, String recordId) {
        BlockManager blockManager = new RecordIdBlock();
        BlockDescriptor descriptor = blockManager.getBlockDescriptor(formClass.getId(), RecordIdBlock.FIELD_NAME, recordNumber);

        updateBlock(blockManager, descriptor, recordNumber, TextValue.valueOf(recordId));
    }

    /**
     * Updates the field's block to the given record id
     *
     * @param recordNumber one-based record number
     * @param fieldValues map of values to update
     */
    public void updateFields(int recordNumber, Map<ResourceId, FieldValue> fieldValues) {
        for (FormField field : formClass.getFields()) {
            if (fieldValues.containsKey(field.getId())) {
                updateFieldBlock(recordNumber, field, fieldValues.get(field.getId()));
            }
        }
    }

    public void updateParentId(int recordNumber, String parentRecordId) {
        BlockManager blockManager = new StringBlock(new ViewBuilderFactory.TextFieldReader());
        BlockDescriptor descriptor = blockManager.getBlockDescriptor(formClass.getId(), "@parent", recordNumber);

        updateBlock(blockManager, descriptor, recordNumber, TextValue.valueOf(parentRecordId));
    }


    private void updateFieldBlock(int recordNumber, FormField field, FieldValue fieldValue) {

        BlockManager blockManager = BlockFactory.get(field.getType());
        BlockDescriptor descriptor = blockManager.getBlockDescriptor(formId, field.getName(), recordNumber);

        updateBlock(blockManager, descriptor, recordNumber, fieldValue);
    }


    public void updateTombstone(int recordNumber) {

        TombstoneBlock tombstone = new TombstoneBlock();
        BlockDescriptor descriptor = tombstone.getBlockDescriptor(formId, recordNumber);
        Entity blockEntity = getOrCreateBlock(descriptor, descriptor.key());

        tombstone.markDeleted(blockEntity, descriptor.getOffset(recordNumber));

        blockMap.put(blockEntity.getKey(), blockEntity);
    }

    private void updateBlock(BlockManager blockManager, BlockDescriptor descriptor, int recordIndex, FieldValue fieldValue) {

        Entity blockEntity = getOrCreateBlock(descriptor, descriptor.key());

        Entity updatedEntity = blockManager.update(blockEntity, descriptor.getOffset(recordIndex), fieldValue);

        if(updatedEntity != null) {
            blockMap.put(updatedEntity.getKey(), updatedEntity);
        }
    }

    private Entity getOrCreateBlock(BlockDescriptor descriptor, Key blockKey) {
        Entity blockEntity = blockMap.get(blockKey);
        if(blockEntity == null) {
            try {
                blockEntity = datastore.get(transaction, blockKey);
            } catch (EntityNotFoundException e) {
                blockEntity = null;
            }
        }
        if(blockEntity == null) {
            blockEntity = new Entity(descriptor.key());
        }
        return blockEntity;
    }

    public Collection<Entity> getUpdatedBlocks() {
        return blockMap.values();
    }
}
