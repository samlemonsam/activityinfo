package org.activityinfo.ui.client.store.offline;

import org.activityinfo.indexedb.ObjectKey;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.hamcrest.Matcher;
import org.junit.Test;

import static org.junit.Assert.*;


public class RecordStoreTest {

    @Test
    public void testSortOrder() {
        String recordKey0 = key("B", "c0");
        String recordKey1 = key("C", "c0");
        String recordKey2 = key("C", "c2");

        String lowerBound = RecordStore.formLower(ResourceId.valueOf("C"));
        String upperBound = RecordStore.formUpper(ResourceId.valueOf("C"));

        assertTrue(ObjectKey.compareKeys(recordKey0, lowerBound) < 0);
        assertTrue(ObjectKey.compareKeys(lowerBound, recordKey1) < 0);
        assertTrue(ObjectKey.compareKeys(recordKey2, upperBound) < 0);

    }

    private String key(String formId, String recordId) {
        return RecordStore.key(new RecordRef(ResourceId.valueOf(formId), ResourceId.valueOf(recordId)));
    }
}