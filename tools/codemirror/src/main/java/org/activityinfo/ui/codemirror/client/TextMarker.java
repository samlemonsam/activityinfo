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

import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface TextMarker {

    /**
     * to remove the mark
     */
    void clear();

    /**
     * @return a {from, to} object (both holding document positions), indicating the current position of the
     * marked range, or undefined if the marker is no longer in the document
     */
    Range find();

    /**
     *  call if you've done something that might change the size of the marker (for example changing the content of
     *  a replacedWith node), and want to cheaply update the display
     */
    void changed();
}