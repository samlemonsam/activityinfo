package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;

public class DoubleValueArray {

    public static final int BYTES = 8;

    /**
     * Updates a contiguous set of 4 double precision to NaN
     *
     * @return true if the block has been updated
     */
    public static boolean update4(Entity blockEntity, String propertyName, int index) {

        Blob blob = (Blob) blockEntity.getProperty(propertyName);

        // Values at the end are assumed to be NaN
        int capacity = ValueArrays.length(blob, BYTES);
        if((index + 3) >= capacity) {
            return false;

        } else {
            byte[] bytes = ensureCapacity(blob, index + 3);
            set(bytes, index + 0, Double.NaN);
            set(bytes, index + 1, Double.NaN);
            set(bytes, index + 2, Double.NaN);
            set(bytes, index + 3, Double.NaN);
            return true;
        }
    }

    /**
     * Updates a contiguous set of 4 double precision to the given four values.
     *
     * @return true if the block has been updated
     */
    public static boolean update4(Entity blockEntity, String propertyName, int index, double v0, double v1, double v2, double v3) {

        Blob blob = (Blob) blockEntity.getProperty(propertyName);

        byte[] bytes = ensureCapacity(blob, index + 3);
        set(bytes, index + 0, v0);
        set(bytes, index + 1, v1);
        set(bytes, index + 2, v2);
        set(bytes, index + 3, v3);

        return true;
    }


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

    public static Blob update(Blob values, int index, double value1, double value2) {
        byte[] bytes = ensureCapacity(values, index + 1);
        set(bytes, index, value1);
        set(bytes, index, value2);
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
