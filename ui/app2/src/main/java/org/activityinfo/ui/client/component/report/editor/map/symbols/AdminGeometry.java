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
package org.activityinfo.ui.client.component.report.editor.map.symbols;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public final class AdminGeometry extends JavaScriptObject {

    protected AdminGeometry() {
    }

    public native int getZoomFactor() /*-{
      return this.zoomFactor;
    }-*/;

    public native int getNumLevels() /*-{
      return this.numLevels;
    }-*/;

    public native JsArray<AdminEntityGeometry> getEntities() /*-{
      return this.entities;
    }-*/;

    public static final native AdminGeometry fromJson(String input) /*-{
      return eval('(' + input + ')');
    }-*/;

}
