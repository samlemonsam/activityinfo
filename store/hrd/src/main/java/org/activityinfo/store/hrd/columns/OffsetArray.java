package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;

/**
 * An array of 16-bit offsets encoded as one-based little-endian byte array.
 *
 * <p>Can be updated without deserialization and
 * re-serialization. Missing offsets are encoded as zero.
 *
 * <p>Unallocated elements at the end of the array are considered to be missing (zero)</p>
 */
public class OffsetArray {


    static final String OFFSETS_PROPERTY = "values";

    public static Blob update(Blob values, int index, char offset) {
        if(offset == 0) {
            return updateToZero(values, index);
        } else {
            byte[] bytes = ValueArrays.ensureCapacity(values, index, ValueArrays.UINT16);
            ValueArrays.setChar(bytes, index, offset);

            return new Blob(bytes);
        }
    }

    /**
     * Zeros out the value at the given index. Unallocated values are assumed to be zero,
     * so if the array is only updated if it is already long enough to include the given index.
     *
     * @param values the value blob
     * @param index the index within the blob
     */
    public static Blob updateToZero(Blob values, int index) {
        if(values == null) {
            return null;
        }
        byte[] bytes = values.getBytes();

        if(bytes.length >= ValueArrays.requiredSize(index, ValueArrays.UINT16)) {
            return new Blob(bytes);
        }

        int pos = index * ValueArrays.UINT16;
        bytes[pos] = 0;
        bytes[pos+1] = 0;

        return new Blob(bytes);
    }

    public static int length(byte[] bytes) {
        return bytes.length / ValueArrays.UINT16;
    }

    public static int length(Blob values) {
        if(values == null) {
            return 0;
        } else {
            return length(values.getBytes());
        }
    }

    /**
     * Retrieves the one-based offset from the byte array. Zero if the offset is missing.
     */
    public static int get(byte[] bytes, int index) {
        int pos = index * ValueArrays.UINT16;

        return (char) ((bytes[pos+1] << 8) | (bytes[pos] & 0xFF));
    }

    /**
     * Updates the offset array property of a block.
     *
     * @param blockEntity
     * @param recordOffset
     * @param value
     *
     * @return true if the offset array was updated, or false if there was no change.
     */
    static boolean updateOffset(Entity blockEntity, int recordOffset, char value) {

        Blob values = (Blob) blockEntity.getProperty(OFFSETS_PROPERTY);
        int existingLength = length(values);

        if(value == 0 && recordOffset >= existingLength) {
            /* No updated needed */
            return false;

        } else {
            values = update(values, recordOffset, value);
            blockEntity.setProperty(OFFSETS_PROPERTY, values);

            return true;
        }
    }
}
