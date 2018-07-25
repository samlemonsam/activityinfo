package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.StringArrayColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.query.shared.columns.StringReader;

import java.util.BitSet;
import java.util.Iterator;

public class StringBlock implements BlockManager {

    private static final int BLOCK_SIZE = 1024;


    private final StringReader reader;
    private final String poolProperty;
    private final String offsetProperty;

    public StringBlock(String fieldName, StringReader reader) {
        this.reader = reader;
        this.poolProperty = fieldName + ":strings";
        this.offsetProperty = fieldName + ":offset";
    }

    @Override
    public int getBlockSize() {
        return BLOCK_SIZE;
    }

    @Override
    public String getBlockType() {
        return "string";
    }

    @Override
    public int getMaxFieldSize() {
        return 2;
    }

    @Override
    public Entity update(Entity blockEntity, int recordOffset, FieldValue fieldValue) {

        // Map this string to an index in our string pool, or zero, if the field value is missing
        char stringIndex = StringPools.findOrInsertStringInPool(blockEntity, poolProperty, toString(fieldValue));

        // Now update the value in the array of "pointers" into our string panel
        if(OffsetArray.updateOffset(blockEntity, offsetProperty, recordOffset,  stringIndex)) {
            return blockEntity;

        } else {
            return null;
        }
    }

    private String toString(FieldValue fieldValue) {
        if(fieldValue == null) {
            return null;
        } else {
            return reader.readString(fieldValue);
        }
    }


    @Override
    public ColumnView buildView(FormEntity header, TombstoneIndex tombstones, Iterator<Entity> blockIterator, String component) {
        String[] values = new String[header.getRecordCount()];
        while (blockIterator.hasNext()) {
            Entity block = blockIterator.next();
            int blockIndex = (int)(block.getKey().getId() - 1);
            int blockStart = blockIndex * getBlockSize();

            // Which records have been deleted?
            BitSet deleted = tombstones.getDeletedBitSet(blockStart, BLOCK_SIZE);

            // Adjust start position depending on the number of records that have been deleted
            // in preceding blocks.
            int targetIndex = blockStart - tombstones.countDeletedBefore(blockStart);

            // Now fill the portion of the array needed
            String[] pool = StringPools.toArray(block, poolProperty);
            if (pool.length > 0) {

                byte[] offsets = ((Blob) block.getProperty(offsetProperty)).getBytes();
                int offsetCount = OffsetArray.length(offsets);

                for (int i = 0; i < offsetCount; i++) {
                    if (!deleted.get(i)) {
                        int offset = OffsetArray.get(offsets, i);
                        if (offset != 0) {
                            values[targetIndex] = pool[offset - 1];
                        }
                        targetIndex++;
                    }
                }
            }
        }
        return new StringArrayColumnView(values);
    }

}
