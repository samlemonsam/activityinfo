package org.activityinfo.core.client.type.converter;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.i18n.client.NumberFormat;
import org.activityinfo.core.shared.type.converter.CoordinateParser;

public class JsCoordinateNumberFormatter implements CoordinateParser.NumberFormatter {

    public static final JsCoordinateNumberFormatter INSTANCE = new JsCoordinateNumberFormatter();
    

    private NumberFormat dddFormat;
    private NumberFormat shortFracFormat;
    private NumberFormat intFormat;
    private char decimalSeparator;
    private char localizedZeroDigit;

    public JsCoordinateNumberFormatter() {
        dddFormat = NumberFormat.getFormat("+0.000000;-0.000000");
        shortFracFormat = NumberFormat.getFormat("0.00");
        intFormat = NumberFormat.getFormat("0");
        String decimalSeparatorString = LocaleInfo.getCurrentLocale().getNumberConstants().decimalSeparator();
        if(decimalSeparatorString.length() != 1) {
            throw new AssertionError("Decimal separator is longer than expected");
        }
        String zeroDigitString = LocaleInfo.getCurrentLocale().getNumberConstants().zeroDigit();
        localizedZeroDigit = zeroDigitString.charAt(0);
        decimalSeparator = decimalSeparatorString.charAt(0);
    }


    @Override
    public String formatDDd(double value) {
        return dddFormat.format(value);
    }

    @Override
    public String formatShortFraction(double value) {
        return shortFracFormat.format(value);
    }

    @Override
    public String formatInt(double value) {
        return intFormat.format(value);
    }

    @Override
    public double parseDouble(String string) {
        return NumberFormat.getDecimalFormat().parse(string);
    }

    @Override
    public char getDecimalSeparator() {
        return decimalSeparator;
    }

    @Override
    public boolean isDigit(char c) {
        return (c >= '0' && c <= '9') || (c >= localizedZeroDigit && c <= (localizedZeroDigit+9));
    }
}
