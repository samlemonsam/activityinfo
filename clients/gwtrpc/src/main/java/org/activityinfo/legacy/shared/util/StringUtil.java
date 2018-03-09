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
package org.activityinfo.legacy.shared.util;

import com.google.common.base.Preconditions;

/**
 * @author yuriyz on 10/14/2015.
 */
public class StringUtil {

    private StringUtil() {
    }

    public static String truncate(String s) {
        return truncate(s, 255);
    }

    public static String truncate(String s, int maxLength) {
        if (s == null) {
            return null;
        }
        Preconditions.checkState(maxLength > 0);
        return s.substring(0, Math.min(s.length(), maxLength));
    }
}
