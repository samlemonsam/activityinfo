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
package org.activityinfo.model.form;

import com.google.common.collect.Lists;
import org.activityinfo.model.resource.ResourceId;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * @author yuriyz on 01/22/2016.
 */
public class FormInstanceComparatorTest {

    private static final ResourceId CLASS_ID = ResourceId.valueOf("id");

    @Test
    public void doubleComparatorTest() {

        List<FormInstance> list = testList();

        Collections.sort(list, FormInstanceComparators.doubleComparator(ResourceId.valueOf("key1")));

        assertEquals("3", list.get(0).getId().asString());
        assertEquals("2", list.get(1).getId().asString());
        assertEquals("1", list.get(2).getId().asString());
    }

    @Test
    public void stringComparatorTest() {

        List<FormInstance> list = testList();

        Collections.sort(list, FormInstanceComparators.stringComparator(ResourceId.valueOf("key2")));

        assertEquals("2", list.get(0).getId().asString());
        assertEquals("3", list.get(1).getId().asString());
        assertEquals("1", list.get(2).getId().asString());
    }

    private List<FormInstance> testList() {
        final ResourceId key1 = ResourceId.valueOf("key1");
        final ResourceId key2 = ResourceId.valueOf("key2");

        FormInstance instance1 = newInstance(1);
        instance1.set(key1, 3);
        instance1.set(key2, "d");

        FormInstance instance2 = newInstance(2);
        instance2.set(key1, 2);
        instance2.set(key2, "b");

        FormInstance instance3 = newInstance(3);
        instance3.set(key1, 1);
        instance3.set(key2, "c");

        return Lists.newArrayList(instance1, instance2, instance3);
    }

    private FormInstance newInstance(int id) {
        return new FormInstance(ResourceId.valueOf(Integer.toString(id)), CLASS_ID);
    }
}
