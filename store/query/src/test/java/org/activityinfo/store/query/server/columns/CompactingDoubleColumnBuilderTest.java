package org.activityinfo.store.query.server.columns;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.query.shared.PendingSlot;
import org.activityinfo.store.query.shared.columns.DoubleReader;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CompactingDoubleColumnBuilderTest {

    public static final DoubleReader DUMMY = new DoubleReader() {
        @Override
        public double read(FieldValue value) {
            return 0;
        }
    };

    @Test
    public void number8() {

        CompactingDoubleColumnBuilder builder = new CompactingDoubleColumnBuilder(new PendingSlot<ColumnView>(), DUMMY);
        builder.onNext(-10);
        builder.onNext(55);
        builder.onNext(31);
        builder.onNext(Double.NaN);
        builder.onNext(-3);
        builder.onNext(-10 + 254);
        builder.onNext(0);

        IntColumnView8 result = (IntColumnView8) builder.build();

        assertEquivalent(result, builder.buildDouble());
    }


    @Test
    public void comprehensiveByte8() {

        CompactingDoubleColumnBuilder builder = new CompactingDoubleColumnBuilder(new PendingSlot<ColumnView>(), DUMMY);

        builder.onNext(Double.NaN);
        for (int i = 0; i < IntColumnView8.MAX_RANGE; i++) {
            builder.onNext(i);
        }
        ColumnView result = builder.build();

        assertThat(result, instanceOf(IntColumnView8.class));
        assertEquivalent(result, builder.buildDouble());
    }

    @Test
    public void comprehensiveByte16() {

        CompactingDoubleColumnBuilder builder = new CompactingDoubleColumnBuilder(new PendingSlot<ColumnView>(), DUMMY);

        builder.onNext(Double.NaN);
        for (int i = 0; i < IntColumnView16.MAX_RANGE; i++) {
            builder.onNext(i);
        }
        ColumnView result = builder.build();

        assertThat(result, instanceOf(IntColumnView16.class));
        assertEquivalent(result, builder.buildDouble());
    }

    @Test
    public void number16() {

        CompactingDoubleColumnBuilder builder = new CompactingDoubleColumnBuilder(new PendingSlot<ColumnView>(), DUMMY);
        builder.onNext(-10);
        builder.onNext(55);
        builder.onNext(31);
        builder.onNext(Double.NaN);
        builder.onNext(-3);
        builder.onNext(-23555);
        builder.onNext(3455);
        builder.onNext(0);

        ColumnView result = builder.build();

        assertThat(result, instanceOf(IntColumnView16.class));

        assertEquivalent(result, builder.buildDouble());
    }

    @Test
    public void sparse() {

        CompactingDoubleColumnBuilder builder = new CompactingDoubleColumnBuilder(new PendingSlot<ColumnView>(), DUMMY);
        for (int i = 0; i < 10_000; i++) {
            if(i == 3) {
                builder.onNext(44.5);
            } else if(i == 1431) {
                builder.onNext(1e6);
            } else {
                builder.onNext(Double.NaN);
            }
        }

        ColumnView result = builder.build();

        assertThat(result, instanceOf(SparseNumberColumnView.class));

        assertEquivalent(result, builder.buildDouble());
    }



    private void assertEquivalent(ColumnView view, ColumnView reference) {

        compare(view, reference);

        compareSelection(new int[] {-1, -1, 4, 5, 4, 3, 3, -1}, view, reference);

    }

    private void compareSelection(int[] selectedRows, ColumnView view, ColumnView reference) {
        compare(view.select(selectedRows), reference.select(selectedRows));
    }

    private void compare(ColumnView view, ColumnView reference) {
        assertThat("numRows", view.numRows(), equalTo(reference.numRows()));

        for (int i = 0; i < view.numRows(); i++) {
            assertThat("missing(" + i + ")", view.isMissing(i), equalTo(reference.isMissing(i)));
            if(!reference.isMissing(i)) {
                assertThat("value(" + i + ")", view.getDouble(i), equalTo(reference.getDouble(i)));
            } else {
                assertTrue("isNa(value(" + i + "))", Double.isNaN(view.getDouble(i)));
            }
        }
    }

}