package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.google.common.base.Charsets;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.query.shared.columns.StringReader;

import java.util.Arrays;

public class StringBlock implements BlockManager {

    private static final int INDEX_LENGTH = 4;

    private final StringReader reader;

    public StringBlock(StringReader reader) {
        this.reader = reader;
    }

    @Override
    public int getBlockSize() {
        return 1000;
    }

    @Override
    public Entity update(Entity blockEntity, int relativeIndex, FieldValue fieldValue) {

        int stringIndex = findOrInsertStringInPool(blockEntity, fieldValue);

        // Now update the value in the array of "pointers" into our string panel
        Blob values = (Blob) blockEntity.getProperty("values");
        byte[] valueArray = values.getBytes();

        // Expand if necessary
        int requiredLength = (relativeIndex + 1) * INDEX_LENGTH;
        if(valueArray.length < requiredLength) {
            valueArray = Arrays.copyOf(valueArray, requiredLength);
        }

        setInt(valueArray, relativeIndex, relativeIndex * INDEX_LENGTH, stringIndex);

        blockEntity.setProperty("values", new Blob(valueArray));

        return blockEntity;
    }

    private void setInt(byte[] valueArray, int relativeIndex, int index, int value) {
        valueArray[index] =  (byte) (value >> 24);
        valueArray[relativeIndex * INDEX_LENGTH + 1] =  (byte) (value >> 16);
        valueArray[relativeIndex * INDEX_LENGTH + 2] = (byte) (value >> 8);
        valueArray[relativeIndex * INDEX_LENGTH + 3] = (byte)value;
    }

    private int findOrInsertStringInPool(Entity blockEntity, FieldValue fieldValue) {

        // If this value is missing, encode the value as zero

        if(fieldValue == null) {
            return 0;
        }

        // Otherwise, look it up in the string pool

        String newValue = reader.readString(fieldValue);
        byte[] newValueBytes = newValue.getBytes(Charsets.UTF_8);

        Blob strings = (Blob) blockEntity.getProperty("strings");

        // Walk through the pool until we find matching bytes

        int index = StringPools.find(strings.getBytes(), newValueBytes);
        if(index < 0) {
            byte[] updatedPool = StringPools.appendString(strings.getBytes(), newValueBytes);
            index = StringPools.size(updatedPool);
            if(index == StringPools.MAX_SIZE) {
                throw new UnsupportedOperationException("TODO");
            }
            blockEntity.setProperty("strings", updatedPool);
        }

        return index + 1;
    }


}
