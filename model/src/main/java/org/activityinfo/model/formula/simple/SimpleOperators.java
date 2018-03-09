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
package org.activityinfo.model.formula.simple;

import com.google.common.collect.Lists;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SimpleOperators {

    private static final ArrayList<SimpleOperator> EQUALITY_OPERATORS = Lists.newArrayList(
            SimpleOperator.EQUALS,
            SimpleOperator.NOT_EQUALS);

    private static final ArrayList<SimpleOperator> QUANTITY_OPERATORS = Lists.newArrayList(
            SimpleOperator.EQUALS,
            SimpleOperator.NOT_EQUALS,
            SimpleOperator.LESS_THAN,
            SimpleOperator.LESS_THAN_EQUAL,
            SimpleOperator.GREATER_THAN,
            SimpleOperator.GREATER_THAN_EQUAL);

    private static final ArrayList<SimpleOperator> INCLUDE_OPERATORS = Lists.newArrayList(
            SimpleOperator.INCLUDES,
            SimpleOperator.NOT_INCLUDES);

    public static List<SimpleOperator> forType(FieldType type) {
        if(type instanceof TextType) {
            return EQUALITY_OPERATORS;
        } else if(type instanceof QuantityType) {
            return QUANTITY_OPERATORS;
        } else if(type instanceof EnumType) {
            EnumType enumType = (EnumType) type;
            if(enumType.getCardinality() == Cardinality.SINGLE) {
                return EQUALITY_OPERATORS;
            } else {
                return INCLUDE_OPERATORS;
            }
        }
        return Collections.emptyList();
    }

}
