package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Blob;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 * An array of 32-bit signed integer values, encoded as little-endian byte array. Missing values encoded as
 * {@link Integer#MIN_VALUE}
 */
public class IntValueArray {

    public static final int MISSING = Integer.MIN_VALUE;
    public static final double MIN_VALUE = Integer.MIN_VALUE + 1;
    public static final double MAX_VALUE = Integer.MAX_VALUE;

    public static final int BYTES = 4;

    /**
     * Return {@code true} if this double value can be encoded in this value array.
     */
    public static boolean accepts(double value) {

        // We encode missing values as Integer.MIN_VALUE

        if (Double.isNaN(value)) {
            return true;
        }

        // Is the value integral?
        if (value != Math.floor(value) || Double.isInfinite(value)) {
            return false;
        }

        if (value < MIN_VALUE || value > MAX_VALUE) {
            return false;
        }

        return true;
    }

    /**
     * Converts a double-precision floating point value to a 32-bit integer suitable for
     * storage in this array. {@code NaN} values are encoded as {@code MISSING} integers.
     */
    public static int toInt(double doubleValue) {
        if(Double.isNaN(doubleValue)) {
            return MISSING;
        } else {
            return (int)doubleValue;
        }
    }

    static double toDouble(int intValue) {
        if(intValue == MISSING) {
            return Double.NaN;
        } else {
            return (double)intValue;
        }
    }

    /**
     * @return the number of integer elements in this value array.
     */
    public static int length(Blob valueArray) {
        return ValueArrays.length(valueArray, BYTES);
    }


    /**
     * Returns a view of this array as an {@link IntBuffer}
     */
    public static IntBuffer asBuffer(Blob valueArray) {
        ByteBuffer buffer = ByteBuffer.wrap(valueArray.getBytes());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.asIntBuffer();
    }

    /**
     * Allocates, if necessary, a larger array to hold up to the element index. Unused space is
     * initialized with the missing value (Integer.MIN_VALUE)
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
    public static Blob update(Blob values, int index, int value) {
        byte[] bytes = ensureCapacity(values, index);
        setInt(bytes, index, value);
        return new Blob(bytes);
    }

    private static void setInt(byte[] bytes, int index, int value) {
        int pos = index * BYTES;
        bytes[pos++] = (byte) value;
        bytes[pos++] = (byte) (value >> 8);
        bytes[pos++] = (byte) (value >> 16);
        bytes[pos  ] = (byte) (value >> 24);
    }
}
