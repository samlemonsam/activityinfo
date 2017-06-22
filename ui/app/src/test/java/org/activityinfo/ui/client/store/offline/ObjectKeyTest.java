package org.activityinfo.ui.client.store.offline;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ObjectKeyTest {

    @Test
    public void upperLower() {

        String[] recordKey0 = new String[] { "B", "c0" };
        String[] recordKey1 = new String[] { "C", "c0" };
        String[] recordKey2 = new String[] { "C", "c2" };

        String[] lowerBound = new String[] { "C" };
        String[] upperBound = new String[] { "C", "\uFFFF" };

        assertTrue(ObjectKey.compareKeys(recordKey0, lowerBound) < 0);
        assertTrue(ObjectKey.compareKeys(lowerBound, recordKey1) < 0);
        assertTrue(ObjectKey.compareKeys(recordKey2, upperBound) < 0);


    }

}