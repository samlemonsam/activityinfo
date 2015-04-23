package org.activityinfo.ui.client.util;
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

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author yuriyz on 1/27/14.
 */
public class GwtUtil {

    public static final int DEFAULT_COLUMN_WIDTH_IN_EM = 10;

    /**
     * Avoid instance creation.
     */
    private GwtUtil() {
    }

    /**
     * Sometimes "hardcode" is not as good solution if column width is big, here we are trying to increase
     * column width if column header is bigger then default value. (Indeed we have room for improvements for this dummy algorithm ;))
     *
     * @param columnHeader column header string
     * @return width of column in em
     */
    public static int columnWidthInEm(String columnHeader) {
        int width = columnHeader.length() / 2;
        return width < DEFAULT_COLUMN_WIDTH_IN_EM ? DEFAULT_COLUMN_WIDTH_IN_EM : width;
    }

    public static void setVisibleInline(boolean visible, Element... elements) {
        if (elements != null) {
            for (Element element : elements) {
                if (visible) {
                    element.removeClassName("hidden");
                    element.addClassName("show-inline");
                } else {
                    element.removeClassName("show-inline");
                    element.addClassName("hidden");
                }
            }
        }
    }

    public static void setVisible(boolean visible, Element... elements) {
        if (elements != null) {
            for (Element element : elements) {
                if (visible) {
                    element.removeClassName("hidden");
                    element.addClassName("show");
                } else {
                    element.removeClassName("show");
                    element.addClassName("hidden");
                }
            }
        }
    }

    public static String valueWithTooltip(String value) {
        return "<span qtip='" + value + "'>" + value + "</span>";
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
