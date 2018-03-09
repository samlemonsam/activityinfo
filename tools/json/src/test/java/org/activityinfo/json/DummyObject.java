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
package org.activityinfo.json;

import jsinterop.annotations.JsType;

/**
 * Because this object is annotated with @JsType, the GWT
 * compiler will preserve the name of the fields, so it can
 * be used directly for JSON serialization, both to a string
 * and to a plain old JS object that can be stored directly in
 * IndexedDB.
 */
@JsType
public class DummyObject {

    public boolean b;
    public int i;
    public double d;
    public String s;
    public String s0;
    public JsonValue object;

    private double[] da;

    private double privateField = 42;


}
