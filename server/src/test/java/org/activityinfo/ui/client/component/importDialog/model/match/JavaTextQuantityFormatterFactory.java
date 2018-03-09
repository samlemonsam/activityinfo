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
package org.activityinfo.ui.client.component.importDialog.model.match;

import org.activityinfo.ui.client.component.importDialog.model.type.formatter.QuantityFormatter;
import org.activityinfo.ui.client.component.importDialog.model.type.formatter.QuantityFormatterFactory;

import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Creates a formatter for a field using the standard Java API
 */
public class JavaTextQuantityFormatterFactory implements QuantityFormatterFactory {
    @Override
    public QuantityFormatter create() {
        final NumberFormat format = NumberFormat.getNumberInstance();
        return new QuantityFormatter() {
            @Override
            public String format(Double value) {
                return format.format(value);
            }

            @Override
            public Double parse(String valueAsString) {
                try {
                    // consider strings with '-' not at the start as invalid
                    // e.g. "2012-12-18" is not 2012.0
                    if (valueAsString.indexOf("-") > 1 || valueAsString.contains("/")) {
                        return null;
                    }
                    return format.parse(valueAsString).doubleValue();
                } catch (ParseException e) {
                    return null;
                }
            }
        };
    }
}
