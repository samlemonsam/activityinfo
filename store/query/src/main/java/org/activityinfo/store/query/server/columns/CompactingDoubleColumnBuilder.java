package org.activityinfo.store.query.server.columns;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.DoubleArrayColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.query.shared.PendingSlot;
import org.activityinfo.store.query.shared.columns.DoubleReader;
import org.activityinfo.store.spi.CursorObserver;

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

        if(integers) {
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
