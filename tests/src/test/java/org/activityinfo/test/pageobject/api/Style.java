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
package org.activityinfo.test.pageobject.api;

public class Style {

    private final String style;

    public Style(String text) {
        this.style = text;
    }

    public int getLeft() {
        return parseValue("left");
    }

    public int getTop() {
        return parseValue("top");
    }

    public int getWidth() {
        return parseValue("width");
    }

    public int getHeight() {
        return parseValue("height");
    }

    public boolean hasValue(String attribute) {
        int i = style.indexOf(attribute + ":");
        return i >= 0;
    }
    
    private int parseValue(String attribute) {
        int i = style.indexOf(attribute + ":");
        i += attribute.length() + 1;

        // skip whitespace
        while(style.charAt(i) == ' ') {
            i++;
        }

        int start = i;
        while(Character.isDigit(style.charAt(i))) {
            i++;
        }

        return Integer.parseInt(style.substring(start, i));
    }

    public boolean hasHeight() {
        return hasValue("height");
    }
    
    public boolean hasWidth() {
        return hasValue("width");
    }

    
}
