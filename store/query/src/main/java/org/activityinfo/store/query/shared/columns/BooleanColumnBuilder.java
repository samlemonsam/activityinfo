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
package org.activityinfo.store.query.shared.columns;

import org.activityinfo.model.query.BitSetColumnView;
import org.activityinfo.model.query.BitSetWithMissingView;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.store.query.shared.PendingSlot;
import org.activityinfo.store.spi.CursorObserver;

import java.util.BitSet;

public class BooleanColumnBuilder implements CursorObserver<FieldValue> {

    private final PendingSlot<ColumnView> result;

    private BitSet values = new BitSet();
    private BitSet missing = new BitSet();
    private int index = 0;

    public BooleanColumnBuilder(PendingSlot<ColumnView> result) {
        this.result = result;
    }


    @Override
    public void onNext(FieldValue value) {
        if (value instanceof BooleanFieldValue) {
            values.set(index, value == BooleanFieldValue.TRUE);
        } else {
            missing.set(index, true);
        }
        index++;
    }

    @Override
    public void done() {
        int numRows = index;
        if (missing.isEmpty()) {
            result.set(new BitSetColumnView(numRows, values));
        } else {
            result.set(new BitSetWithMissingView(numRows, values, missing));
        }
    }
}
