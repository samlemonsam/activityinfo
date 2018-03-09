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
package org.activityinfo.model.type;

import org.activityinfo.json.JsonValue;

public class MissingFieldType implements FieldType {

    public static final MissingFieldType INSTANCE = new MissingFieldType();

    public static final FieldTypeClass TYPE_CLASS = new FieldTypeClass() {

        @Override
        public String getId() {
            return "missing";
        }

        @Override
        public FieldType createType() {
            return null;
        }
    };

    private MissingFieldType() {
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

    @Override
    public FieldValue parseJsonValue(JsonValue value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T accept(FieldTypeVisitor<T> visitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUpdatable() {
        return false;
    }

    /**
     *
     * @return the singleton instance for this type
     */
    private Object readResolve() {
        return INSTANCE;
    }

}
