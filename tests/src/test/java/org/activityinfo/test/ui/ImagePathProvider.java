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
package org.activityinfo.test.ui;

import org.apache.commons.lang3.SystemUtils;

/**
 * @author yuriyz on 11/24/2015.
 */
public class ImagePathProvider {

    public static String path(String imageName) {
        String path = ImagePathProvider.class.getResource(imageName).toString();
        if (path.startsWith("file:/")) {
            path = path.substring("file:/".length());
        }
        if (SystemUtils.IS_OS_UNIX && !path.startsWith("/")) {
            path = "/" + path;
        }
        if (SystemUtils.IS_OS_WINDOWS && path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }
}
