package org.activityinfo.store.query.impl.builders;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.DoubleArrayColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.query.impl.PendingSlot;
import org.activityinfo.store.query.impl.views.NumberColumnView16;
import org.activityinfo.store.query.impl.views.NumberColumnView8;
import org.activityinfo.store.query.impl.views.SparseNumberColumnView;
import org.activityinfo.store.spi.CursorObserver;

public class DoubleColumnBuilder implements CursorObserver<FieldValue> {

    private final PendingSlot<ColumnView> result;

    private final DoubleArrayList values = new DoubleArrayList();

    private DoubleReader reader;
    private double min = Double.POSITIVE_INFINITY;
    private double max = Double.NEGATIVE_INFINITY;
    private boolean integers = true;
    private int missingCount = 0;

    public DoubleColumnBuilder(PendingSlot<ColumnView> result, DoubleReader reader) {
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

        if(integers) {
            int minInt = (int)min;
            int maxInt = (int)max;
            int range = maxInt - minInt;
            if(range <= NumberColumnView8.MAX_RANGE) {
                return new NumberColumnView8(values.elements(), numRows, minInt);
            }
            if(range <= NumberColumnView16.MAX_RANGE) {
                return new NumberColumnView16(values.elements(), numRows, minInt);
            }
        }
        return buildDouble();
    }

    @VisibleForTesting
    DoubleArrayColumnView buildDouble() {
        return new DoubleArrayColumnView(values.elements(), values.size());
    }
}
