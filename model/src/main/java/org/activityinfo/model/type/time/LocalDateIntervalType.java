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
package org.activityinfo.model.type.time;

import org.activityinfo.json.JsonValue;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldTypeVisitor;
import org.activityinfo.model.type.FieldValue;

/**
 * Value type that represents a continuous interval between two {@link org.activityinfo.model.type.time.LocalDate}s,
 * starting on {@code startDate}, inclusive, and ending on {@code endDate}, inclusive.
 */
public class LocalDateIntervalType implements FieldType {

    public static final FieldTypeClass TYPE_CLASS = new FieldTypeClass() {
        @Override
        public String getId() {
            return "localDateInterval";
        }

        @Override
        public FieldType createType() {
            return LocalDateIntervalType.INSTANCE;
        }
    };

    public static final LocalDateIntervalType INSTANCE = new LocalDateIntervalType();

    private LocalDateIntervalType() {
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

    @Override
    public FieldValue parseJsonValue(JsonValue value) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public <T> T accept(FieldTypeVisitor<T> visitor) {
        return visitor.visitLocalDateInterval(this);
    }

    @Override
    public boolean isUpdatable() {
        return true;
    }

    /**
     *
     * @return the singleton instance for this type
     */
    private Object readResolve() {
        return INSTANCE;
    }


}
