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

import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.primitive.TextValue;

public class EqualFunction extends ComparisonOperator {

    public static final EqualFunction INSTANCE = new EqualFunction();

    public EqualFunction() {
        super("==");
    }

    public String getLabel() {
        return "Equal";
    }

    @Override
    protected boolean apply(FieldValue a, FieldValue b) {

        if(a instanceof TextValue && b instanceof ReferenceValue) {
            return compare(((ReferenceValue) b), ((TextValue) a));
        }

        if(a instanceof ReferenceValue && b instanceof TextValue) {
            return compare(((ReferenceValue) a), ((TextValue) b));
        }

        // Check for relevancy calculation
        if(a instanceof EnumValue && b instanceof TextValue) {
            // A is EnumValue and B is String Literal
            return relevancyCalculation(((EnumValue) a).getValueId().asString(),((TextValue) b).asString());
        } else if (a instanceof TextValue && b instanceof EnumValue) {
            // A is String Literal and B is EnumValue
            return relevancyCalculation(((TextValue) a).asString(),((EnumValue) b).getValueId().asString());
        }
        return a.equals(b);
    }

    private boolean relevancyCalculation(String a, String b) {
        return a.equals(b);
    }

    private boolean compare(ReferenceValue refValue, TextValue textValue) {
        if(refValue.getReferences().size() == 1) {
            RecordRef recordRef = refValue.getReferences().iterator().next();
            return recordRef.getRecordId().asString().equals(textValue.asString());
        }
        return false;
    }

    @Override
    protected boolean apply(double x, double y) {
        return x == y;
    }
}
