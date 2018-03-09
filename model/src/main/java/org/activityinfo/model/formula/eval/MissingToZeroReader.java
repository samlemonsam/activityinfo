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
package org.activityinfo.model.formula.eval;

import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;

/**
 * Wraps a QuantityType field reader, replacing missing values with zeros to support
 * legacy behavior.
 */
public class MissingToZeroReader<InstanceT> implements FieldReader<InstanceT> {

    private final FieldReader<InstanceT> reader;
    private final QuantityType type;

    public MissingToZeroReader(FieldReader<InstanceT> reader) {
        this.reader = reader;
        this.type = (QuantityType) reader.getType();
    }

    @Override
    public FieldValue readField(InstanceT record) {
        return new Quantity(0);
    }

    @Override
    public FieldType getType() {
        return type;
    }
}
