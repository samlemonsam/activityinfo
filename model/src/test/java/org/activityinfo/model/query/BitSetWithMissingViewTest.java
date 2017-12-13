package org.activityinfo.model.query;

import org.junit.Test;

import java.util.BitSet;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class BitSetWithMissingViewTest {

    @Test
    public void selectWithMissing() {

        BitSet values = new BitSet();
        BitSet missing = new BitSet();
        values.set(0);
        values.set(2);
        missing.set(5);


        ColumnView columnView = new BitSetWithMissingView(6, values, missing);

        int selectedRows[] = new int[] { 1, 1, -1, 0, 5};

        ColumnView selectedView = columnView.select(selectedRows);

        assertThat(selectedView.numRows(), equalTo(5));
        assertThat(selectedView.getBoolean(0), equalTo(ColumnView.FALSE));
        assertThat(selectedView.getBoolean(1), equalTo(ColumnView.FALSE));
        assertThat(selectedView.getBoolean(2), equalTo(ColumnView.NA));
        assertThat(selectedView.getBoolean(3), equalTo(ColumnView.TRUE));
        assertThat(selectedView.getBoolean(4), equalTo(ColumnView.NA));
    }


    @Test
    public void selectWithNoMissing() {

        BitSet values = new BitSet();
        BitSet missing = new BitSet();
        values.set(0);
        values.set(2);
        missing.set(5);

        ColumnView columnView = new BitSetWithMissingView(6, values, missing);

        int selectedRows[] = new int[] { 1, 1, 2 };

        ColumnView selectedView = columnView.select(selectedRows);

        assertThat(selectedView.numRows(), equalTo(3));
        assertThat(selectedView.getBoolean(0), equalTo(ColumnView.FALSE));
        assertThat(selectedView.getBoolean(1), equalTo(ColumnView.FALSE));
        assertThat(selectedView.getBoolean(2), equalTo(ColumnView.TRUE));
    }

}