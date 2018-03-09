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
package org.activityinfo.ui.client.catalog;

import com.google.common.base.Optional;
import com.google.gwt.place.shared.Place;

public class CatalogPlace extends Place {
    private Optional<String> parentId;

    public CatalogPlace(String parentId) {
        this.parentId = Optional.of(parentId);
    }

    public CatalogPlace(Optional<String> parentId) {
        this.parentId = parentId;
    }

    public Optional<String> getParentId() {
        return parentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CatalogPlace that = (CatalogPlace) o;

        return parentId.equals(that.parentId);
    }

    @Override
    public int hashCode() {
        return parentId.hashCode();
    }
}
