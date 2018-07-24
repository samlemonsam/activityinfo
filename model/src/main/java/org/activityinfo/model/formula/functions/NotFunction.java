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
package org.activityinfo.model.formula.functions;

import org.activityinfo.model.query.BitSetColumnView;
import org.activityinfo.model.query.BitSetWithMissingView;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.BooleanType;

import java.util.BitSet;
import java.util.List;

public class NotFunction extends FormulaFunction implements ColumnFunction {

    public static final NotFunction INSTANCE = new NotFunction();

    private NotFunction() {}

    @Override
    public String getId() {
        return "!";
    }

    @Override
    public String getLabel() {
        return getId();
    }

    @Override
    public BooleanFieldValue apply(List<FieldValue> arguments) {
        checkArity(arguments,1);
        boolean x = Casting.toBoolean(arguments.get(0));
        return BooleanFieldValue.valueOf(!x);
    }

    @Override
    public FieldType resolveResultType(List<FieldType> argumentTypes) {
        return BooleanType.INSTANCE;
    }

    @Override
    public ColumnView columnApply(int numRows, List<ColumnView> arguments) {

        ColumnView argument = arguments.get(0);

        BitSet bitSet = new BitSet();
        BitSet missing = new BitSet();

        for (int i = 0; i < argument.numRows(); i++) {
            int value = argument.getBoolean(i);
            if(value == ColumnView.NA) {
                missing.set(i);
            } else {
                bitSet.set( i, value != ColumnView.TRUE );
            }
        }

        if(missing.isEmpty()) {
            return new BitSetColumnView(numRows, bitSet);
        } else {
            return new BitSetWithMissingView(numRows, bitSet, missing);
        }
    }
}
