package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Blob;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;

public class DoubleValueArray {

    public static final int BYTES = 8;

    private DoubleValueArray() {}

    /**
     * Allocates, if necessary, a larger array to hold up to the element index. Unused space is
     * initialized with NaN
     *
     * @param blob The existing values blob, or {@code null} if it is still uninitialized.
     * @param index the value index to update
     */
    public static byte[] ensureCapacity(Blob blob, int index) {
        int originalLength = ValueArrays.length(blob, BYTES);
        byte[] updatedArray = ValueArrays.ensureCapacity(blob, index, BYTES);

        // Fill empty spaces with NaN, which has the byte layout of [0, 0, 0, 0, 0, 0, -8, 127]
        int pos = originalLength * BYTES;
        while(pos < updatedArray.length) {
            updatedArray[pos + 6] = (byte) -8;
            updatedArray[pos + 7] = (byte) 127;
            pos += 8;
        }
        return updatedArray;
    }

    public static Blob update(Blob values, int index, double value) {
        byte[] bytes = ensureCapacity(values, index);
        set(bytes, index, value);
        return new Blob(bytes);
    }

    private static void set(byte[] bytes, int index, double value) {
        long longValue = Double.doubleToRawLongBits(value);
        int pos = index * BYTES;
        bytes[pos++] = (byte)longValue;
        bytes[pos++] = (byte)(longValue >> 8);
        bytes[pos++] = (byte)(longValue >> 16);
        bytes[pos++] = (byte)(longValue >> 24);
        bytes[pos++] = (byte)(longValue >> 32);
        bytes[pos++] = (byte)(longValue >> 40);
        bytes[pos++] = (byte)(longValue >> 48);
        bytes[pos  ] = (byte)(longValue >> 56);
    }

    public static int length(Blob valueArray) {
        return ValueArrays.length(valueArray, BYTES);
    }

    /**
     * Returns a view of this array as an {@link DoubleBuffer}
     */
    public static DoubleBuffer asBuffer(Blob valueArray) {
        ByteBuffer buffer = ByteBuffer.wrap(valueArray.getBytes());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.asDoubleBuffer();
    }

}
