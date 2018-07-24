package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Blob;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.vividsolutions.jts.util.AssertionFailedException;
import org.junit.Test;

import java.util.Arrays;

public class ValueArraysTest {

    @Test
    public void testNaN() {

        long longValue = Double.doubleToRawLongBits(Double.NaN);
        byte[] bytes = Longs.toByteArray(longValue);

        System.out.println(Arrays.toString(bytes));
    }

    @Test
    public void testIntMax() {

        byte[] bytes = Ints.toByteArray(Integer.MIN_VALUE);

        System.out.println(Arrays.toString(bytes));
    }

    @Test
    public void testIntValues() {
        Blob valueArray = IntValueArray.update(null, 3, 42);

        checkArray(ValueArrays.toIntArray(valueArray),
                IntValueArray.MISSING,
                IntValueArray.MISSING,
                IntValueArray.MISSING,
                42);

        valueArray = IntValueArray.update(valueArray, 0, 96);

        checkArray(ValueArrays.toIntArray(valueArray),
                96,
                IntValueArray.MISSING,
                IntValueArray.MISSING,
                42);

        valueArray = IntValueArray.update(valueArray, 5, 3024);

        checkArray(ValueArrays.toIntArray(valueArray),
                96,
                IntValueArray.MISSING,
                IntValueArray.MISSING,
                42,
                IntValueArray.MISSING,
                3024);
    }

    private void checkArray(int[] actual, int... expected) {
        if(!Arrays.equals(actual, expected)) {
            System.out.println("Expected: "+ Arrays.toString(expected));
            System.out.println("Actual: " + Arrays.toString(actual));
            throw new AssertionFailedException();
        }
    }
}