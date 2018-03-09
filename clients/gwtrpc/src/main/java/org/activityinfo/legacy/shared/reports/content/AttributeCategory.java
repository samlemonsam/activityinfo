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
package org.activityinfo.legacy.shared.reports.content;

import com.google.common.base.Preconditions;

public class AttributeCategory implements DimensionCategory {

    private String value;
    private int sortOrder;

    private AttributeCategory() {

    }

    public AttributeCategory(String value, int sortOrder) {
        Preconditions.checkNotNull(value);
        this.value = value;
        this.sortOrder = sortOrder;
    }

    @Override
    public Integer getSortKey() {
        return sortOrder;
    }

    @Override
    public String getLabel() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AttributeCategory other = (AttributeCategory) obj;
        return value.equals(other.value);
    }
}
