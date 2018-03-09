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
package org.activityinfo.ui.client.component.importDialog.validation.cells;

import com.google.common.base.Function;
import org.activityinfo.ui.client.component.importDialog.model.type.formatter.QuantityFormatter;

import javax.annotation.Nullable;

/**
 * Renders a QUANTITY field value to SafeHtml
 */
public class QuantityRenderer implements Function<Object, String> {

    private QuantityFormatter formatter;

    public QuantityRenderer(QuantityFormatter formatter) {
        this.formatter = formatter;
    }

    @Nullable
    @Override
    public String apply(@Nullable Object input) {
        return input == null ? null : formatter.format((Double)input);
    }
}
