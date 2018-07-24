package org.activityinfo.model.formula.functions;

import org.activityinfo.model.query.BooleanColumnView;
import org.activityinfo.model.query.ColumnView;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class NotFunctionTest {

    @Test
    public void test() {

        ColumnView input = new BooleanColumnView(new int[] { 0, 1, 1 });
        ColumnView result = NotFunction.INSTANCE.columnApply(input.numRows(), Collections.singletonList(input));

        assertThat(result.getBoolean(0), equalTo(1));
        assertThat(result.getBoolean(1), equalTo(0));
        assertThat(result.getBoolean(2), equalTo(0));
    }

    @Test
    public void missing() {

        ColumnView input = new BooleanColumnView(new int[] { 0, 1, 1, 0, ColumnView.NA, 0});
        ColumnView result = NotFunction.INSTANCE.columnApply(input.numRows(), Collections.singletonList(input));

        assertThat(result.getBoolean(0), equalTo(1));
        assertThat(result.getBoolean(1), equalTo(0));
        assertThat(result.getBoolean(2), equalTo(0));
        assertThat(result.getBoolean(3), equalTo(1));
        assertThat(result.getBoolean(4), equalTo(ColumnView.NA));
        assertThat(result.getBoolean(5), equalTo(1));
    }

}