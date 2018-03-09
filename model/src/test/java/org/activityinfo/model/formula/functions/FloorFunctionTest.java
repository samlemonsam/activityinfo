package org.activityinfo.model.formula.functions;

import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertThat;

public class FloorFunctionTest {

    @Test
    public void test() {
        assertThat(FloorFunction.INSTANCE.apply(1.6), Matchers.equalTo(1.0));
        assertThat(FloorFunction.INSTANCE.apply(1.5), Matchers.equalTo(1.0));
        assertThat(FloorFunction.INSTANCE.apply(1.4), Matchers.equalTo(1.0));

        assertThat(FloorFunction.INSTANCE.apply(-1.6), Matchers.equalTo(-2.0));
        assertThat(FloorFunction.INSTANCE.apply(-1.5), Matchers.equalTo(-2.0));
        assertThat(FloorFunction.INSTANCE.apply(-1.4), Matchers.equalTo(-2.0));

        assertThat(FloorFunction.INSTANCE.apply(  Collections.<FieldValue>singletonList(new Quantity(1.5))),
                Matchers.<FieldValue>equalTo(new Quantity(1.0)));
        assertThat(FloorFunction.INSTANCE.apply(  Collections.<FieldValue>singletonList(new Quantity(-1.5))),
                Matchers.<FieldValue>equalTo(new Quantity(-2.0)));
    }

}