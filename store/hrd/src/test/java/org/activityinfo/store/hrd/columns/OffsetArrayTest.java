package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.EmbeddedEntity;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class OffsetArrayTest {

    @Test
    public void test() {

        EmbeddedEntity block = new EmbeddedEntity();

        assertTrue(OffsetArray.updateOffset(block, "myoffset", 0, (char)19));
        assertTrue(OffsetArray.updateOffset(block, "myoffset", 1, (char)534));
        assertFalse(OffsetArray.updateOffset(block, "myoffset", 2, (char)0));
        assertTrue(OffsetArray.updateOffset(block, "myoffset", 3, (char)2));
        assertTrue(OffsetArray.updateOffset(block, "myoffset", 4, (char)1024));

        byte[] bitset = ((Blob) block.getProperty("myoffset")).getBytes();


        assertThat(OffsetArray.get(bitset, 0), equalTo(19));
        assertThat(OffsetArray.get(bitset, 1), equalTo(534));
        assertThat(OffsetArray.get(bitset, 2), equalTo(0));
        assertThat(OffsetArray.get(bitset, 3), equalTo(2));
        assertThat(OffsetArray.get(bitset, 4), equalTo(1024));

    }

}