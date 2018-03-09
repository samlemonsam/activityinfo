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
package org.activityinfo.test.config;

import com.google.common.base.Enums;
import com.google.common.base.Optional;

import java.util.Arrays;

/**
 * Defines a configuration property of enumerated type
 */
public class EnumConfigProperty<T extends Enum<T>> {

    private final Class<T> enumClass;
    private final ConfigProperty property;

    public EnumConfigProperty(String name, String description, Class<T> enumClass) {
        this.enumClass = enumClass;
        this.property = new ConfigProperty(name, description + " " + Arrays.toString(enumClass.getEnumConstants()));
    }

    public T get() {
        Optional<T> value = Enums.getIfPresent(enumClass, property.get().toUpperCase());
        if(!value.isPresent()) {
            throw new ConfigurationError(String.format(
                    "Invalid value for system property '%s': '%s'. Expected: " +
                            Arrays.toString(enumClass.getEnumConstants())));

        }
        return value.get();
    }
}
