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

import com.google.common.collect.Lists;
import org.activityinfo.model.query.*;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.query.shared.PendingSlot;
import org.activityinfo.store.spi.CursorObserver;

import java.util.List;

public class StringColumnBuilder implements CursorObserver<FieldValue> {

    private final PendingSlot<ColumnView> result;


    private List<String> values = Lists.newArrayList();

    // Keep track of some statistics to
    private StringStatistics stats = new StringStatistics();


    private StringReader reader;

    public StringColumnBuilder(PendingSlot<ColumnView> result, StringReader reader) {
        this.result = result;
        this.reader = reader;
    }

    @Override
    public void onNext(FieldValue value) {
        String string = null;
        if(value != null) {
            string = reader.readString(value);
        }
        stats.update(string);
        values.add(string);
    }

    @Override
    public void done() {
        if(stats.isEmpty()) {
            result.set(new EmptyColumnView(ColumnType.STRING, values.size()));

        } else if(stats.isConstant()) {
            result.set(new ConstantColumnView(values.size(), values.get(0)));

        } else {
            result.set(new StringArrayColumnView(values));
        }
    }
}
