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

import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.BooleanType;

import java.util.List;

/**
 * @author yuriyz on 9/3/14.
 */
public class NotContainsAnyFunction extends FormulaFunction {

    public static final NotContainsAnyFunction INSTANCE = new NotContainsAnyFunction();

    private NotContainsAnyFunction() {
    }

    @Override
    public String getId() {
        return "notContainsAny";
    }

    @Override
    public String getLabel() {
        return "Excludes Any";
    }

    @Override
    public BooleanFieldValue apply(List<FieldValue> arguments) {
        return BooleanFieldValue.valueOf(!ContainsAnyFunction.INSTANCE.apply(arguments).asBoolean());
    }

    @Override
    public FieldType resolveResultType(List<FieldType> argumentTypes) {
        return BooleanType.INSTANCE;
    }
}