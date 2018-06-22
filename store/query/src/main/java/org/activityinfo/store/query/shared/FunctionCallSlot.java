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
package org.activityinfo.store.query.shared;

import com.google.common.collect.Lists;
import org.activityinfo.model.formula.diagnostic.FormulaException;
import org.activityinfo.model.formula.functions.ColumnFunction;
import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.EmptyColumnView;
import org.activityinfo.store.spi.Slot;

import java.util.List;

class FunctionCallSlot implements Slot<ColumnView> {
    private final List<Slot<ColumnView>> argumentSlots;
    private ColumnFunction function;

    public FunctionCallSlot(ColumnFunction function, List<Slot<ColumnView>> argumentSlots) {
        this.function = function;
        this.argumentSlots = argumentSlots;

    }

    @Override
    public ColumnView get() {
        List<ColumnView> arguments = Lists.newArrayList();
        for (Slot<ColumnView> argument : argumentSlots) {
            ColumnView view = argument.get();
            if (view == null) {
                throw new IllegalStateException();
            }
            arguments.add(view);
        }
        try {
            return function.columnApply(arguments.get(0).numRows(), arguments);
        } catch (FormulaException e) {
            int numRows = arguments.get(0).numRows();
            return new EmptyColumnView(ColumnType.STRING, numRows);
        }
    }
}
