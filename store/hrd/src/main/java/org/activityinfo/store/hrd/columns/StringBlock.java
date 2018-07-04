package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.StringArrayColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.hrd.entity.FormColumnStorage;
import org.activityinfo.store.query.shared.columns.StringReader;

import java.util.Iterator;

public class StringBlock implements BlockManager {

    private static final String POOL_PROPERTY = "strings";

    private final StringReader reader;

    public StringBlock(StringReader reader) {
        this.reader = reader;
    }

    @Override
    public int getBlockSize() {
        return 1000;
    }

    @Override
    public Entity update(Entity blockEntity, int recordOffset, FieldValue fieldValue) {

        // Map this string to an index in our string pool, or zero, if the field value is missing
        char stringIndex = StringPools.findOrInsertStringInPool(blockEntity, POOL_PROPERTY, toString(fieldValue));

        // Now update the value in the array of "pointers" into our string panel
        if(OffsetArray.updateOffset(blockEntity, recordOffset, stringIndex)) {
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
    public ColumnView buildView(FormColumnStorage header, TombstoneIndex tombstones, Iterator<Entity> blockIterator) {
        String[] values = new String[header.getRecordCount()];
        while (blockIterator.hasNext()) {
            Entity block = blockIterator.next();
            int blockIndex = (int)(block.getKey().getId() - 1);
            int blockStart = blockIndex * getBlockSize();

            // Adjust start position depending on the number of records that have been deleted
            // in preceding blocks.
            int targetIndex = blockStart - tombstones.countDeletedBefore(blockStart);

            // Now fill the portion of the array needed
            String[] pool = StringPools.toArray((Blob) block.getProperty(POOL_PROPERTY));
            if(pool.length > 0) {
                byte[] offsets = ((Blob)block.getProperty(OffsetArray.OFFSETS_PROPERTY)).getBytes();
                int offsetCount = OffsetArray.length(offsets);

                for (int i = 0; i < offsetCount; i++) {
                    if (!tombstones.isDeleted(blockStart + i)) {
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
