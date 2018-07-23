package org.activityinfo.store.hrd.op;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.store.hrd.columns.*;
import org.activityinfo.store.hrd.entity.ColumnDescriptor;
import org.activityinfo.store.hrd.entity.FieldDescriptor;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.query.shared.columns.ViewBuilderFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ColumnBlockUpdater {
    private final FormEntity formEntity;
    private final ResourceId formId;
    private final FormClass formClass;

    /**
     * Map of changed blocks
     */
    private final Map<BlockId, Entity> blockMap = new HashMap<>();

    private final DatastoreService datastore;
    private final Transaction transaction;

    public ColumnBlockUpdater(FormEntity formEntity, FormClass formClass, Transaction transaction) {
        this.formEntity = formEntity;
        this.formId = formClass.getId();
        this.formClass = formClass;
        this.datastore = DatastoreServiceFactory.getDatastoreService();
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
        BlockId descriptor = blockManager.getBlockId(formClass.getId(), RecordIdBlock.BLOCK_NAME, recordNumber);

        formEntity.setTailIdBlockVersion(formEntity.getVersion());

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
        BlockManager blockManager = new StringBlock("parent", new ViewBuilderFactory.TextFieldReader());
        BlockId descriptor = blockManager.getBlockId(formClass.getId(), "@parent", recordNumber);

        updateBlock(blockManager, descriptor, recordNumber, TextValue.valueOf(parentRecordId));
    }

    private void updateFieldBlock(int recordNumber, FormField field, FieldValue fieldValue) {

        BlockManager blockManager = BlockFactory.get(field);

        FieldDescriptor fieldDescriptor = formEntity.getFieldDescriptor(field.getName());
        fieldDescriptor.setVersion(formEntity.getVersion());

        ColumnDescriptor block = blockForField(field, fieldDescriptor, blockManager);

        BlockId descriptor = blockManager.getBlockId(formId, block.getColumnId(), recordNumber);

        block.setBlockVersion(descriptor.getBlockIndex(), formEntity.getVersion());

        updateBlock(blockManager, descriptor, recordNumber, fieldValue);
    }

    private ColumnDescriptor blockForField(FormField field, FieldDescriptor fieldDescriptor, BlockManager blockManager) {
        if(fieldDescriptor.hasBlockAssignment()) {
            return formEntity.getFieldBlock(fieldDescriptor.getColumnId());
        } else {
            return assignBlock(fieldDescriptor, field, blockManager);
        }
    }

    private ColumnDescriptor assignBlock(FieldDescriptor fieldDescriptor, FormField field, BlockManager blockManager) {
        for (ColumnDescriptor block : formEntity.getBlockColumns().values()) {
            if(blockManager.canBeAssignedTo(block)) {
                block.addField(field.getName());
                fieldDescriptor.setColumnId(block.getColumnId());
                return block;
            }
        }

        // Need to allocate a new block
        ColumnDescriptor block = new ColumnDescriptor();
        block.setColumnId("col" + (formEntity.getBlockColumns().size() + 1));
        block.setBlockType(blockManager.getBlockType());
        block.setRecordCount(blockManager.getRecordCount());
        block.addField(field.getName());

        formEntity.addFieldBlock(block);

        fieldDescriptor.setColumnId(block.getColumnId());

        return block;
    }


    public void updateTombstone(int recordNumber) {

        TombstoneBlock tombstone = new TombstoneBlock();
        BlockId blockId = tombstone.getBlockDescriptor(formId, recordNumber);
        Entity blockEntity = getOrCreateBlock(blockId);

        tombstone.markDeleted(blockEntity, blockId.getOffset(recordNumber, TombstoneBlock.BLOCK_SIZE));

        formEntity.setTombstoneBlockVersion(blockId.getBlockIndex(), formEntity.getVersion());

        blockMap.put(blockId, blockEntity);
    }

    private void updateBlock(BlockManager blockManager, BlockId blockId, int recordIndex, FieldValue fieldValue) {

        Entity blockEntity = getOrCreateBlock(blockId);

        Entity updatedEntity = blockManager.update(blockEntity,
                blockId.getOffset(recordIndex, blockManager.getRecordCount()), fieldValue);

        if(updatedEntity != null) {
            blockMap.put(blockId, updatedEntity);
        }
    }

    private Entity getOrCreateBlock(BlockId blockId) {
        Entity blockEntity = blockMap.get(blockId);
        if(blockEntity == null) {
            try {
                blockEntity = datastore.get(transaction, blockId.key());
            } catch (EntityNotFoundException e) {
                blockEntity = null;
            }
        }
        if(blockEntity == null) {
            blockEntity = new Entity(blockId.key());
        }
        return blockEntity;
    }

    public Collection<Entity> getUpdatedBlocks() {
        return blockMap.values();
    }

    public void cacheUpdatedBlocks() {
        MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
        for (Map.Entry<BlockId, Entity> entry : blockMap.entrySet()) {
            memcache.put(entry.getKey().memcacheKey(formEntity.getVersion()), entry.getValue());
        }
    }
}
