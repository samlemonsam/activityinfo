package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Blob;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class LongValueArrayTest {

    @Test
    public void getSet() {

        Blob blob = null;
        blob = LongValueArray.update(blob, 2, 1577836803236L);
        blob = LongValueArray.update(blob, 9, 1577836807103L);
        blob = LongValueArray.update(blob, 9, 1577836807104L);
        blob = LongValueArray.update(blob, 100, 42L);

        assertThat(LongValueArray.get(blob, 2), equalTo(1577836803236L));
        assertThat(LongValueArray.get(blob, 9), equalTo(1577836807104L));
        assertThat(LongValueArray.get(blob, 100), equalTo(42L));


    }
}