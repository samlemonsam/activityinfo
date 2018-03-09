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
package org.activityinfo.ui.codemirror.client;

import com.google.gwt.core.client.JavaScriptObject;

public class MarkOptions extends JavaScriptObject {


    protected MarkOptions() {
    }

    public static native MarkOptions create() /*-{
        return {};
    }-*/;

    /**
     * Assigns a CSS class to the marked stretch of text.
     */
    public final native void setClassName(String className) /*-{
        this.className = className;
    }-*/;

    /**
     * When given, will give the nodes created for this span a HTML title attribute with the given value.
     */
    public final native void setTitle(String title) /*-{
        this.title = title;
    }-*/;


}
