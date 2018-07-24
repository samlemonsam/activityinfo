package org.activityinfo.store.hrd.columns;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class IntValueArrayTest {

    @Test
    public void getSet() {

        byte[] bytes = IntValueArray.ensureCapacity(null, 8);
        IntValueArray.set(bytes, 0, 0);
        IntValueArray.set(bytes, 1, -500);
        IntValueArray.set(bytes, 2, 65339);
        IntValueArray.set(bytes, 3, IntValueArray.MISSING);
        IntValueArray.set(bytes, 4, Integer.MAX_VALUE);

        assertThat(IntValueArray.get(bytes, 0), equalTo(0));
        assertThat(IntValueArray.get(bytes, 1), equalTo(-500));
        assertThat(IntValueArray.get(bytes, 2), equalTo(65339));
        assertThat(IntValueArray.get(bytes, 3), equalTo(IntValueArray.MISSING));
        assertThat(IntValueArray.get(bytes, 4), equalTo(Integer.MAX_VALUE));

    }

}