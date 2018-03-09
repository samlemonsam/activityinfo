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
package org.activityinfo.test.pageobject.web.design.designer;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @author yuriyz on 06/19/2015.
 */
public enum DesignerFieldPropertyType {
    CODE("code"),
    LABEL("label"),
    DESCRIPTION("description"),
    RELEVANCE("relevance"),
    REQUIRED("required"),
    VISIBLE("visible");

    private final String value;

    DesignerFieldPropertyType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Set<DesignerFieldPropertyType> fromCommaSeparateString(String propertyTypesSeparatedByComma) {
        Set<DesignerFieldPropertyType> result = Sets.newHashSet();
        for (String property : propertyTypesSeparatedByComma.split("\\s*,\\s*")) {
            result.add(fromValue(property));
        }
        return result;
    }

    public static DesignerFieldPropertyType fromValue(String value) {
        for (DesignerFieldPropertyType type : values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown field property value: " + value);
    }
}
