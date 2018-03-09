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
package org.activityinfo.legacy.shared.reports.model.typeadapter;

import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.content.EntityCategory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Alex Bertram
 */
public class CategoryAdapter extends XmlAdapter<CategoryAdapter.Category, DimensionCategory> {

    public static class Category {

        @XmlAttribute
        private Integer id;
    }

    @Override
    public DimensionCategory unmarshal(Category category) throws Exception {
        if (category.id != null) {
            return new EntityCategory(category.id);
        }
        return null;
    }

    @Override
    public Category marshal(DimensionCategory v) throws Exception {
        return null;
    }
}
