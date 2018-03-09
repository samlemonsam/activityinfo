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

import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.io.match.coord.CoordinateFormatException;
import org.activityinfo.io.match.coord.CoordinateParser;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

public class JreNumberFormats implements CoordinateParser.NumberFormatter {

    
    private final NumberFormat decimalFormat;
    private final NumberFormat shortDecimalFormat;
    private final NumberFormat intFormat;
    
    public JreNumberFormats() {
        decimalFormat = NumberFormat.getInstance(LocaleProxy.getLocale());
        decimalFormat.setMinimumFractionDigits(6);
        decimalFormat.setMaximumFractionDigits(6);
        decimalFormat.setMinimumIntegerDigits(0);
        
        shortDecimalFormat = NumberFormat.getInstance(LocaleProxy.getLocale());
        shortDecimalFormat.setMinimumFractionDigits(2);
        shortDecimalFormat.setMaximumFractionDigits(2);
        shortDecimalFormat.setMinimumIntegerDigits(1);
        
        intFormat = NumberFormat.getIntegerInstance(LocaleProxy.getLocale());
    }

    @Override
    public double parseDouble(String s) {
        try {
            return decimalFormat.parse(s).doubleValue();
        } catch (ParseException e) {
            throw new CoordinateFormatException(e.getMessage());
        }
    }
    
    @Override
    public char getDecimalSeparator() {
        return ((DecimalFormat) decimalFormat).getDecimalFormatSymbols().getDecimalSeparator();
    }

    @Override
    public boolean isDigit(char c) {
        return Character.isDigit(c);
    }

    @Override
    public String formatDDd(double value) {
        if (value > 0) {
            return "+" + decimalFormat.format(value);
        } else {
            return decimalFormat.format(value);
        }
    }

    @Override
    public String formatShortFraction(double value) {
        return shortDecimalFormat.format(value);
    }

    @Override
    public String formatInt(double value) {
        return intFormat.format(value);
    }
}
