package org.activityinfo.store.hrd.columns;


import com.google.appengine.api.datastore.Blob;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Routines to treat datastore Blobs as arrays of unsigned integers, encoded
 * in little-endian format.
 */
public class ValueArrays {

    public static final int UINT16 = 2;
    public static final int REAL64 = 8;

    /**
     * @param values The existing values blob, or {@code null} if it is still uninitialized.
     * @param index the value index to update
     * @param elementSize the size, in bytes, of the value elements
     * @return
     */
    public static byte[] ensureCapacity(Blob values, int index, int elementSize) {
        int requiredSize = requiredSize(index, elementSize);
        if(values == null) {
            return new byte[requiredSize];
        }
        byte[] existingArray = values.getBytes();
        if(existingArray.length < requiredSize) {
            return Arrays.copyOf(existingArray, requiredSize);
        }

        return existingArray;
    }


    /**
     * Allocates, if neccessary, a larger array to hold up to the element index. Unused space is
     * initialized with NaN
     *
     * @param values The existing values blob, or {@code null} if it is still uninitialized.
     * @param index the value index to update
     */
    public static byte[] ensureCapacityReal64(Blob values, int index) {
        int originalLength = length(values, REAL64);
        byte[] updatedArray = ensureCapacity(values, index, REAL64);

        // Fill empty spaces with NaN, which has the byte layout of [0, 0, 0, 0, 0, 0, -8, 127]
        int pos = originalLength * REAL64;
        while(pos < updatedArray.length) {
            updatedArray[pos + 6] = (byte) -8;
            updatedArray[pos + 7] = (byte) 127;
            pos += 8;
        }
        return updatedArray;
    }

    /**
     * Calculates the number of bytes required to store a value of the given size at the given index.
     *
     * @param index the zero-based index of the value
     * @param elementSize the size of the elements, in bytes
     * @return the number of bytes required
     */
    public static int requiredSize(int index, int elementSize) {
        return (index + 1) * elementSize;
    }

    public static Blob updateReal64(Blob values, int index, double value) {
        byte[] bytes = ensureCapacityReal64(values, index);
        setDouble(bytes, index, value);
        return new Blob(bytes);
    }

    private static void setDouble(byte[] bytes, int index, double value) {
        long longValue = Double.doubleToRawLongBits(value);
        int pos = index * REAL64;
        bytes[pos++] = (byte)longValue;
        bytes[pos++] = (byte)(longValue >> 8);
        bytes[pos++] = (byte)(longValue >> 16);
        bytes[pos++] = (byte)(longValue >> 24);
        bytes[pos++] = (byte)(longValue >> 32);
        bytes[pos++] = (byte)(longValue >> 40);
        bytes[pos++] = (byte)(longValue >> 48);
        bytes[pos  ] = (byte)(longValue >> 56);
    }


    public static void setChar(byte[] bytes, int index, char value) {
        int pos = index * UINT16;
        bytes[pos] = (byte)value;
        bytes[pos+1] = (byte) (value >> 8);
    }

    public static int length(Blob valueArray, int elementSize) {
        if(valueArray == null) {
            return 0;
        }
        return valueArray.getBytes().length / elementSize;
    }

    public static byte[] allocate(int length, int elementSize) {
        return new byte[requiredSize(length, elementSize)];
    }

    public static ByteBuffer asBuffer(Blob valueArray) {
        return asBuffer(valueArray.getBytes());
    }

    public static ByteBuffer asBuffer(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer;
    }

    public static int[] toIntArray(Blob valueArray) {
        int length = length(valueArray, IntValueArray.BYTES);
        ByteBuffer buffer = asBuffer(valueArray);

        int[] array = new int[length];

        for (int i = 0; i < length; i++) {
            array[i] = buffer.getInt(i * IntValueArray.BYTES);
        }
        return array;
    }
}