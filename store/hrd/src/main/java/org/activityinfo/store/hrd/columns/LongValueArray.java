package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.PropertyContainer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

public class LongValueArray {


    public static final int BYTES = 8;
    private static final long MISSING = 0L;


    /**
     * @return the number of integer elements in this value array.
     */
    public static int length(Blob valueArray) {
        return ValueArrays.length(valueArray, BYTES);
    }

    public static int length(byte[] bytes) {
        return bytes.length / BYTES;
    }

    /**
     * Returns a view of this array as an {@link IntBuffer}
     */
    public static LongBuffer asBuffer(Blob valueArray) {
        ByteBuffer buffer = ByteBuffer.wrap(valueArray.getBytes());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.asLongBuffer();
    }

    /**
     * Allocates, if necessary, a larger array to hold up to the element index. Unused space is
     * initialized with zero
     *
     * @param values The existing values blob, or {@code null} if it is still uninitialized.
     * @param index the value index to update
     */
    public static byte[] ensureCapacity(Blob values, int index) {
        int originalLength = length(values);
        byte[] updatedArray = ValueArrays.ensureCapacity(values, index, BYTES);

        // Fill empty spaces with missing value, which is encoded as 0x80000000
        int pos = originalLength * BYTES;
        while(pos < updatedArray.length) {
            updatedArray[pos+3] = (byte)-128;
            pos += 4;
        }
        return updatedArray;
    }

    /**
     * Updates the value at the given {@code index} in the array, allocating a new, larger
     * array only if necessary.
     *
     * @return
     */
    public static Blob update(Blob values, int index, long value) {
        byte[] bytes = ensureCapacity(values, index);
        set(bytes, index, value);
        return new Blob(bytes);
    }

    public static boolean update(PropertyContainer blockEntity, String property, int index, int value) {
        Blob blob = (Blob) blockEntity.getProperty(property);
        if(value == MISSING && index >= length(blob)) {
            // unallocated values at the end of the array are treated as missing,
            // so no need to grow the array
            return false;
        }

        blockEntity.setProperty(property, update(blob, index, value));

        return true;
    }

    public static void set(byte[] bytes, int index, long value) {
        int pos = index * BYTES;
        for (int i = 7; i >= 0; i--) {
            bytes[pos++] = (byte) (value & 0xffL);
            value >>= 8;
        }
    }

    public static long get(Blob blob, int index) {
        if(blob == null) {
            return MISSING;
        } else {
            return get(blob.getBytes(), index);
        }
    }

    public static long get(byte[] bytes, int index) {
        int pos = index * BYTES;
        if(pos >= bytes.length) {
            return MISSING;
        }
        return (bytes[pos] & 0xFFL)
                | (bytes[pos + 1] & 0xFFL) << 8
                | (bytes[pos + 2] & 0xFFL) << 16
                | (bytes[pos + 3] & 0xFFL) << 24
                | (bytes[pos + 4] & 0xFFL) << 32
                | (bytes[pos + 5] & 0xFFL) << 40
                | (bytes[pos + 6] & 0xFFL) << 48
                | (bytes[pos + 7] & 0xFFL) << 56;

    }

    public static Blob fromInt32(Blob versionMap) {
        if(versionMap == null) {
            return null;
        }
        int lastIndex = IntValueArray.length(versionMap) - 1;
        byte[] bytes = ensureCapacity(versionMap,  lastIndex);
        for (int i = 0; i <= lastIndex ; i++) {
            int int32 = IntValueArray.get(bytes, i);
            if(int32 != IntValueArray.MISSING) {
                set(bytes, i, int32);
            }
        }
        return new Blob(bytes);
    }
}
