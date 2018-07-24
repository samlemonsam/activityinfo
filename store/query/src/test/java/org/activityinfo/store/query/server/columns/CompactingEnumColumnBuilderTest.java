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

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.store.spi.PendingSlot;
import org.junit.Test;

import java.util.Objects;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class CompactingEnumColumnBuilderTest {

    @Test
    public void buildCompact() {

        EnumItem a = new EnumItem(ResourceId.valueOf("a"), "Enumerated Item A");
        EnumItem b = new EnumItem(ResourceId.valueOf("b"), "Enumerated Item B");
        EnumItem c = new EnumItem(ResourceId.valueOf("c"), "Enumerated Item C");

        EnumType enumType = new EnumType(Cardinality.SINGLE, a, b, c);

        CompactingEnumColumnBuilder builder = new CompactingEnumColumnBuilder(new PendingSlot<ColumnView>(), enumType);

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