package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.common.base.Charsets;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.StringArrayColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.hrd.entity.FormColumnStorage;
import org.activityinfo.store.query.shared.columns.StringReader;

public class StringBlock implements BlockManager {

    private static final String POOL_PROPERTY = "strings";
    private static final String OFFSETS_PROPERTY = "values";

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

        char stringIndex = findOrInsertStringInPool(blockEntity, fieldValue);

        // Now update the value in the array of "pointers" into our string panel
        Blob values = (Blob) blockEntity.getProperty(OFFSETS_PROPERTY);
        values = OffsetArray.update(values, recordOffset, stringIndex);

        blockEntity.setProperty(OFFSETS_PROPERTY, values);

        return blockEntity;
    }


    private char findOrInsertStringInPool(Entity blockEntity, FieldValue fieldValue) {

        // If this value is missing, encode the value as zero
        if(fieldValue == null) {
            return 0;
        }

        String newValue = reader.readString(fieldValue);

        // Does a string pool already exist?
        Blob strings = (Blob) blockEntity.getProperty(POOL_PROPERTY);
        if(strings == null) {
            blockEntity.setProperty(POOL_PROPERTY, new Blob(StringPools.newPool(newValue)));
            return 1;
        }


        // Otherwise, look it up in the string pool
        // Encode the search string to bytes and match against bytes to avoid having to
        // decode the entire string pool

        byte[] newValueBytes = newValue.getBytes(Charsets.UTF_8);

        // Walk through the pool until we find matching bytes

        int index = StringPools.find(strings.getBytes(), newValueBytes);
        if(index < 0) {
            byte[] updatedPool = StringPools.appendString(strings.getBytes(), newValueBytes);
            index = StringPools.size(updatedPool);
            if(index == StringPools.MAX_SIZE) {
                throw new UnsupportedOperationException("TODO");
            }
            blockEntity.setProperty(POOL_PROPERTY, new Blob(updatedPool));
        }

        return (char) index;
    }


    @Override
    public ColumnView buildView(FormColumnStorage header, QueryResultIterator<Entity> blockIterator) {
        String[] values = new String[header.getRecordCount()];
        while (blockIterator.hasNext()) {
            Entity block = blockIterator.next();
            int blockIndex = (int)(block.getKey().getId() - 1);
            int blockStart = blockIndex * getBlockSize();

            String[] pool = StringPools.toArray((Blob) block.getProperty(POOL_PROPERTY));
            if(pool.length > 0) {
                byte[] offsets = ((Blob)block.getProperty(OFFSETS_PROPERTY)).getBytes();
                int offsetCount = OffsetArray.length(offsets);

                for (int i = 0; i < offsetCount; i++) {
                    int offset = OffsetArray.get(offsets, i);
                    if(offset != 0) {
                        values[blockStart + i] = pool[offset - 1];
                    }
                }
            }
        }
        return new StringArrayColumnView(values);
    }

}
