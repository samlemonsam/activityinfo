package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Blob;

/**
 * An array of 16-bit offsets encoded as one-based little-endian byte array.
 *
 * <p>Can be updated without deserialization and
 * re-serialization. Missing offsets are encoded as zero.
 *
 * <p>Unallocated elements at the end of the array are considered to be missing (zero)</p>
 */
public class OffsetArray {


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
     * @return
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

    /**
     * Retrieves the one-based offset from the byte array. Zero if the offset is missing.
     */
    public static int get(byte[] bytes, int index) {
        int pos = index * ValueArrays.UINT16;

        return (char) ((bytes[pos+1] << 8) | (bytes[pos] & 0xFF));
    }
}
