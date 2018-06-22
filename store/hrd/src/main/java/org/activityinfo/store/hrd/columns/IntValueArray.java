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

    public static int length(Blob valueArray) {
        return ValueArrays.length(valueArray, BYTES);
    }

    public static IntBuffer asBuffer(Blob valueArray) {
        ByteBuffer buffer = ByteBuffer.wrap(valueArray.getBytes());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.asIntBuffer();
    }
}
