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
package org.activityinfo.store.query.server.columns;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.DoubleArrayColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.query.shared.columns.DoubleReader;
import org.activityinfo.store.spi.CursorObserver;
import org.activityinfo.store.spi.PendingSlot;

/**
 * ColumnView builder for quantity-typed fields that adapts the resulting
 * representation of the column based on the contents of the field.
 *
 * <p>For fields that have all integer values and small ranges, the
 * {@link IntColumnView8} and {@link IntColumnView16} implementations are used.</p>
 *
 * <p>For fields that have a large proportion of missing values, the {@link SparseNumberColumnView}
 * is used.</p>
 */
public class CompactingDoubleColumnBuilder implements CursorObserver<FieldValue> {

    private final PendingSlot<ColumnView> result;

    private final DoubleArrayList values = new DoubleArrayList();

    private DoubleReader reader;
    private double min = Double.POSITIVE_INFINITY;
    private double max = Double.NEGATIVE_INFINITY;
    private boolean integers = true;
    private int missingCount = 0;

    public CompactingDoubleColumnBuilder(PendingSlot<ColumnView> result, DoubleReader reader) {
        this.result = result;
        this.reader = reader;
    }

    @Override
    public void onNext(FieldValue value) {
        double x = reader.read(value);
        onNext(x);
    }

    @VisibleForTesting
    void onNext(double x) {
        if(Double.isNaN(x)) {
            missingCount++;
        } else {
            if (x < min) {
                min = x;
            }
            if (x > max) {
                max = x;
            }
            if (integers && !isInteger(x)) {
                integers = false;
            }
        }
        values.add(x);
    }

    private boolean isInteger(double x) {
        return x == Math.floor(x) && !Double.isInfinite(x);
    }

    @Override
    public void done() {
        result.set(build());
    }

    @VisibleForTesting
    ColumnView build() {

        int numRows = values.size();
        double missingRatio = (double)missingCount / (double) numRows;
        if(numRows > 1000 && missingRatio > 0.65) {
            return new SparseNumberColumnView(values.elements(), numRows, missingCount);
        }

        if(integers && (min > Integer.MIN_VALUE) && (max < Integer.MAX_VALUE)) {
            int minInt = (int)min;
            int maxInt = (int)max;
            int range = maxInt - minInt;
            if(range <= IntColumnView8.MAX_RANGE) {
                return new IntColumnView8(values.elements(), numRows, minInt);
            }
            if(range <= IntColumnView16.MAX_RANGE) {
                return new IntColumnView16(values.elements(), numRows, minInt);
            }
        }
        return buildDouble();
    }

    @VisibleForTesting
    DoubleArrayColumnView buildDouble() {
        return new DoubleArrayColumnView(values.elements(), values.size());
    }
}
