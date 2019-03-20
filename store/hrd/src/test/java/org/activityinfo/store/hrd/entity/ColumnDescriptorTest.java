package org.activityinfo.store.hrd.entity;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ColumnDescriptorTest {

    @Test
    public void testWrapAround() {

        // imperfect, but should be enough to bust the cache
        int v1 = ColumnDescriptor.wrapAroundCast(1577836807103L);
        int v2 = ColumnDescriptor.wrapAroundCast(1577836807103L + 1L);

        assertTrue(v1 != v2);

    }

    @Test
    public void serialization() {

        ColumnDescriptor descriptor = new ColumnDescriptor();
        descriptor.setBlockVersion(1, 2);

        FormEntity form = new FormEntity();
        form.setId("a0000");
        form.setVersion(1L);
    }

}