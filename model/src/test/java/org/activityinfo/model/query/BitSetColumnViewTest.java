package org.activityinfo.model.query;

import org.junit.Test;

import java.util.BitSet;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class BitSetColumnViewTest {

    @Test
    public void selectWithMissing() {

        BitSet values = new BitSet();
        values.set(0);
        values.set(2);
        values.set(4);

        BitSetColumnView columnView = new BitSetColumnView(6, values);

        int selectedRows[] = new int[] { 1, 1, -1, 0, 5};

        ColumnView selectedView = columnView.select(selectedRows);

        assertThat(selectedView.numRows(), equalTo(5));
        assertThat(selectedView.getBoolean(0), equalTo(ColumnView.FALSE));
        assertThat(selectedView.getBoolean(1), equalTo(ColumnView.FALSE));
        assertThat(selectedView.getBoolean(2), equalTo(ColumnView.NA));
        assertThat(selectedView.getBoolean(3), equalTo(ColumnView.TRUE));
        assertThat(selectedView.getBoolean(4), equalTo(ColumnView.FALSE));

    }

}