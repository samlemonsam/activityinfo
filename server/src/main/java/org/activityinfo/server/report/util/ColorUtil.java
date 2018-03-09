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
package org.activityinfo.server.report.util;

import com.google.code.appengine.awt.Color;

public final class ColorUtil {

    private ColorUtil() {
    }

    public static Color colorFromString(String color) {
        if (color.startsWith("#")) {
            color = color.substring(1);
        }

        Color result = new Color(0, 255, 0);
        try {
            result = new Color(Integer.parseInt(color));
        } catch (NumberFormatException e) {
            result = Color.decode("0x" + color);
        }
        return result;
    }

    public static int toInteger(String color) {
        return colorFromString(color).getRGB();
    }
}
