package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import org.activityinfo.model.query.BitSetColumnView;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.store.hrd.entity.FormEntity;

import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MultiEnumBlock implements BlockManager {


    private final String bitsetPrefix;

    public MultiEnumBlock(String fieldName) {
        this.bitsetPrefix = fieldName + ":";
    }

    @Override
    public int getRecordCount() {
        return 1024 * 5;
    }

    @Override
    public int getMaxFieldSize() {
        return 2;
    }

    @Override
    public String getBlockType() {
        return "multienum";
    }

    @Override
    public Entity update(Entity blockEntity, int recordOffset, @Nullable FieldValue fieldValue) {

        // Find the existing bitsets for this field
        Set<String> toUnset = new HashSet<>();
        for (String property : blockEntity.getProperties().keySet()) {
            if(property.startsWith(bitsetPrefix)) {
                toUnset.add(property);
            }
        }


        boolean dirty = false;

        if(fieldValue instanceof EnumValue) {
            EnumValue enumValue = (EnumValue) fieldValue;
            for (ResourceId enumId : enumValue.getResourceIds()) {
                String propertyKey = bitsetPrefix + enumId.asString();
                if (BlobBitSet.update(blockEntity, propertyKey, recordOffset, true)) {
                    dirty = true;
                }
                toUnset.remove(propertyKey);
            }
        }

        // Unset any existing bit vectors
        for (String property : toUnset) {
            if (BlobBitSet.update(blockEntity, property, recordOffset, false)) {
                dirty = true;
            }
        }

        if(dirty) {
            return blockEntity;
        } else {
            return null;
        }
    }

    @Override
    public ColumnView buildView(FormEntity header, TombstoneIndex tombstones, Iterator<Entity> blockIterator, String component) {

        String bitSetProperty = bitsetPrefix + component;

        BitSet result = new BitSet();

        while (blockIterator.hasNext()) {
            Entity block = blockIterator.next();
            int blockIndex = (int)(block.getKey().getId() - 1);
            int blockStart = blockIndex * getRecordCount();

            // Which records have been deleted?
            BitSet deleted = tombstones.getDeletedBitSet(blockStart, getRecordCount());

            // Adjust start position depending on the number of records that have been deleted
            // in preceding blocks.
            int targetIndex = blockStart - tombstones.countDeletedBefore(blockStart);

            // Now fill the portion of the array needed
            Blob bitSetBlob = (Blob) block.getProperty(bitSetProperty);
            if(bitSetBlob != null) {
                byte[] blockBitSet = bitSetBlob.getBytes();
                int bitCount = BlobBitSet.length(blockBitSet);

                for (int i = 0; i < bitCount; i++) {
                    if (!deleted.get(blockStart + i)) {
                        boolean value = BlobBitSet.get(blockBitSet, i);
                        if(value) {
                            result.set(targetIndex);
                        }
                        targetIndex++;
                    }
                }
            }
        }
        return new BitSetColumnView(header.getRecordCount() - header.getDeletedCount(), result);
    }
}
