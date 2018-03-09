/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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