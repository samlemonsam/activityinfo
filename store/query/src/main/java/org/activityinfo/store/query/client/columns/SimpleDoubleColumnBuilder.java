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
package org.activityinfo.store.query.client.columns;

import com.google.common.collect.Lists;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.DoubleArrayColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.query.shared.columns.DoubleReader;
import org.activityinfo.store.spi.CursorObserver;
import org.activityinfo.store.spi.PendingSlot;

import java.util.List;

/**
 * Naive implementation of a DoubleColumnBuilder for compilation to JavaScript.
 *
 * <p>This might be improved by using JS typed Arrays, but we need good benchmarks for
 * we can start optimizing.</p>
 */
class SimpleDoubleColumnBuilder implements CursorObserver<FieldValue> {

    private final PendingSlot<ColumnView> result;

    private final List<Double> values = Lists.newArrayList();

    private DoubleReader reader;

    SimpleDoubleColumnBuilder(PendingSlot<ColumnView> result, DoubleReader reader) {
        this.result = result;
        this.reader = reader;
    }

    @Override
    public void onNext(FieldValue value) {
        values.add(reader.read(value));
    }

    @Override
    public void done() {

        double array[] = new double[values.size()];
        for(int i=0;i!=array.length;++i) {
            array[i] = values.get(i);
        }
        result.set(new DoubleArrayColumnView(array));
    }
}
