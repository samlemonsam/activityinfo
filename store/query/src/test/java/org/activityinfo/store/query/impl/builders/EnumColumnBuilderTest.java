package org.activityinfo.store.query.impl.builders;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.store.query.impl.PendingSlot;
import org.activityinfo.store.query.impl.views.DiscreteStringColumnView8;
import org.junit.Test;

import java.util.Objects;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;

public class EnumColumnBuilderTest {

    @Test
    public void buildCompact() {

        EnumItem a = new EnumItem(ResourceId.valueOf("a"), "Enumerated Item A");
        EnumItem b = new EnumItem(ResourceId.valueOf("b"), "Enumerated Item B");
        EnumItem c = new EnumItem(ResourceId.valueOf("c"), "Enumerated Item C");

        EnumType enumType = new EnumType(Cardinality.SINGLE, a, b, c);

        EnumColumnBuilder builder = new EnumColumnBuilder(new PendingSlot<ColumnView>(), enumType);

        for (int i = 0; i < 13; i++) {
            builder.onNext(new EnumValue(a.getId()));
            builder.onNext(new EnumValue(b.getId()));
            builder.onNext(new EnumValue(c.getId()));
            builder.onNext(null);
        }

        ColumnView column8 = builder.build8();
        ColumnView column32 = builder.build32();

        for (int i = 0; i < column32.numRows(); i++) {
            if (!Objects.equals(column8.getString(i), column32.getString(i))) {
                throw new AssertionError("Vectors not equal at index " + i);
            }
        }

        assertThat(builder.build(), instanceOf(DiscreteStringColumnView8.class));

    }

}