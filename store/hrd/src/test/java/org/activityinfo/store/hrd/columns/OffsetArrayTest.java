package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.EmbeddedEntity;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class OffsetArrayTest {

    public static final String PROPERTY = "myoffset";

    @Test
    public void test() {

        EmbeddedEntity block = new EmbeddedEntity();

        assertTrue(OffsetArray.updateOffset(block, PROPERTY, 0, (char)19));
        assertTrue(OffsetArray.updateOffset(block, PROPERTY, 1, (char)534));
        assertFalse(OffsetArray.updateOffset(block, PROPERTY, 2, (char)0));
        assertTrue(OffsetArray.updateOffset(block, PROPERTY, 3, (char)2));
        assertTrue(OffsetArray.updateOffset(block, PROPERTY, 4, (char)1024));

        byte[] bitset = ((Blob) block.getProperty(PROPERTY)).getBytes();


        assertThat(OffsetArray.get(bitset, 0), equalTo(19));
        assertThat(OffsetArray.get(bitset, 1), equalTo(534));
        assertThat(OffsetArray.get(bitset, 2), equalTo(0));
        assertThat(OffsetArray.get(bitset, 3), equalTo(2));
        assertThat(OffsetArray.get(bitset, 4), equalTo(1024));

    }

    @Test
    public void updateZero() {

        EmbeddedEntity block = new EmbeddedEntity();

        OffsetArray.updateOffset(block, PROPERTY, 0, (char)19);
        OffsetArray.updateOffset(block, PROPERTY, 1, (char)534);


        // Update the zero in the beginning of the array
        OffsetArray.updateOffset(block, PROPERTY, 0, (char)0);

        assertThat(OffsetArray.get(block, PROPERTY, 0), equalTo(0));

        // Update the zero at the end of the array
        OffsetArray.updateOffset(block, PROPERTY, 1, (char)0);
        assertThat(OffsetArray.get(block, PROPERTY, 0), equalTo(0));
        assertThat(OffsetArray.get(block, PROPERTY, 1), equalTo(0));

        // Update a zero beyond the allocate range
        OffsetArray.updateOffset(block, PROPERTY, 2, (char)0);
        assertThat(OffsetArray.get(block, PROPERTY, 0), equalTo(0));
        assertThat(OffsetArray.get(block, PROPERTY, 1), equalTo(0));
        byte[] bytes = ((Blob) block.getProperty(PROPERTY)).getBytes();
        assertThat(bytes.length, equalTo(4));
    }

}