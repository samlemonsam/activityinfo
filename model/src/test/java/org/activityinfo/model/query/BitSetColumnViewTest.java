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