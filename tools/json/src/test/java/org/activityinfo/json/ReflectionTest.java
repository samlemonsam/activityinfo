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

import org.junit.Test;

import static org.activityinfo.json.Json.toJson;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class ReflectionTest {

    @Test
    public void testObject() {

        DummyObject o = new DummyObject();
        o.d = 41;
        o.s = "Hello World";
        o.s0 = null;
        o.i = 99;
        o.object = Json.createObject();
        o.object.put("a", "Brave New World");

        JsonValue jo = toJson(o);

        assertThat(jo.getNumber("d"), equalTo(41d));
        assertThat(jo.getString("s"), equalTo("Hello World"));
        assertThat(jo.getString("s0"), nullValue());
        assertThat(jo.getNumber("i"), equalTo(99d));
        assertThat(jo.getNumber("privateField"), equalTo(42d));

        JsonValue joo = jo.get("object");
        assertThat(joo.getString("a"), equalTo("Brave New World"));
    }

    @Test
    public void testBooleanTrue() {
        DummyObject o = new DummyObject();
        o.b = true;

        JsonValue jo = toJson(o);
        assertThat(jo.getBoolean("b"), equalTo(true));
    }

}
