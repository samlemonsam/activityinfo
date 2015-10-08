package org.activityinfo.model.expr.functions;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;

/**
 * @author yuriyz on 10/08/2015.
 */
public class LessFunction extends ComparisonOperator {

    public static final LessFunction INSTANCE = new LessFunction();

    public LessFunction() {
        super("<");
    }

    @Override
    protected boolean apply(FieldValue a, FieldValue b) {
        if (a instanceof Quantity && b instanceof Quantity) {
            Quantity qa = (Quantity) a;
            Quantity qb = (Quantity) b;
            return qa.getValue() < qb.getValue();
        }

        throw new UnsupportedOperationException("Operators are not supported.");
    }
}

