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
package org.activityinfo.store.query.shared.join;

import org.activityinfo.model.resource.ResourceId;

/**
 * Uniquely identifies a foreign key
 */
public class ForeignKeyId {

    private String fieldName;
    private ResourceId rightFormId;

    public ForeignKeyId(String fieldName, ResourceId rightFormId) {
        this.fieldName = fieldName;
        this.rightFormId = rightFormId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public ResourceId getRightFormId() {
        return rightFormId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ForeignKeyId that = (ForeignKeyId) o;

        if (!fieldName.equals(that.fieldName)) return false;
        return rightFormId.equals(that.rightFormId);

    }

    @Override
    public int hashCode() {
        int result = fieldName.hashCode();
        result = 31 * result + rightFormId.hashCode();
        return result;
    }

    public ResourceId getFieldId() {
        return ResourceId.valueOf(fieldName);
    }
}
