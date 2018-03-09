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

@JsType
public class SelectionOptions {

    /**
     * Determines whether the selection head should be scrolled into view. Defaults to true.
     */
    public boolean scroll;


    /**
     * Determines whether the selection history event may be merged with the previous one.
     * When an origin starts with the character +, and the last recorded selection had the same
     * origin and was similar (close in time, both collapsed or both non-collapsed),
     * the new one will replace the old one. When it starts with *, it will always replace the
     * previous event (if that had the same origin). Built-in motion uses the "+move" origin.
     * User input uses the "+input" origin.
     */
    public String origin;


    /**
     * Determine the direction into which the selection endpoints should be adjusted when they fall inside an atomic range.
     * Can be either -1 (backward) or 1 (forward). When not given, the bias will be based on the relative
     * position of the old selection—the editor will try to move further away from that, to prevent getting stuck.
     */
    public int bias;
}
