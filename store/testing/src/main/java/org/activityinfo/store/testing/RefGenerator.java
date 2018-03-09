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
import com.google.common.collect.Lists;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RefGenerator implements Supplier<FieldValue> {

    private final Random random = new Random(92412451L);
    private final List<TestForm> rangeForms;


    public RefGenerator(TestForm... rangeForms) {
        this.rangeForms = Arrays.asList(rangeForms);
    }

    public RefGenerator(Iterable<TestForm> rangeForms) {
        this.rangeForms = Lists.newArrayList(rangeForms);
    }

    @Override
    public FieldValue get() {

        TestForm rangeForm;
        if(rangeForms.size() == 1) {
            rangeForm = rangeForms.get(0);
        } else {
            rangeForm = rangeForms.get(random.nextInt(rangeForms.size()));
        }

        int index = random.nextInt(rangeForm.getRecords().size());
        RecordRef recordRef = rangeForm.getRecords().get(index).getRef();
        return new ReferenceValue(recordRef);
    }
}
