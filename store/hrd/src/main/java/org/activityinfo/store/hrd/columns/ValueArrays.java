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

    /**
     * @param blob The existing values blob, or {@code null} if it is still uninitialized.
     * @param index the value index to update
     * @param elementSize the size, in bytes, of the value elements
     * @return
     */
    public static byte[] ensureCapacity(Blob blob, int index, int elementSize) {
        int requiredSize = requiredSize(index, elementSize);
        if(blob == null) {
            return new byte[requiredSize];
        }
        byte[] existingArray = blob.getBytes();
        if(existingArray.length < requiredSize) {
            return Arrays.copyOf(existingArray, requiredSize);
        }

        return existingArray;
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


    public static void setChar(byte[] bytes, int index, char value) {
        int pos = index * OffsetArray.BYTES;
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
        return new byte[length * elementSize];
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