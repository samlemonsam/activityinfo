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
package org.activityinfo.ui.client.util;

import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author yuriyz on 1/27/14.
 */
public class GwtUtil {

    /**
     * Avoid instance creation.
     */
    private GwtUtil() {
    }

    public static ScrollPanel getScrollAncestor(Widget widget) {
        if (widget != null && widget.getParent() != null) {
            final Widget parent = widget.getParent();
            if (parent instanceof ScrollPanel) {
                return (ScrollPanel) parent;
            } else {
                return getScrollAncestor(parent);
            }
        }
        return null;
    }

    public static boolean isInt(String integer) {
        try {
            Integer.parseInt(integer);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static int getIntSilently(String integer) {
        try {
            return Integer.parseInt(integer);
        } catch (Exception e) {
            return -1;
        }
    }

}
