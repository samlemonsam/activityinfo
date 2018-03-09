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
package org.activityinfo.store.testing;

import com.google.common.base.Supplier;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;

import java.util.*;

/**
 * Generates references to another form, using each one at most once
 */
public class RefKeyGenerator implements Supplier<FieldValue> {


    private final Iterator<RecordRef> stream;

    public RefKeyGenerator(TestForm rangeForm) {
        List<RecordRef> range = new ArrayList<>();
        for (FormInstance record : rangeForm.getRecords()) {
            range.add(new RecordRef(rangeForm.getFormId(), record.getId()));
        }

        Collections.shuffle(range, new Random(19993345L));

        this.stream = range.iterator();
    }

    @Override
    public FieldValue get() {
        if(!stream.hasNext()) {
            throw new IllegalStateException("Cannot generate any more keys - all used");
        }
        return  new ReferenceValue(stream.next());
    }
}
