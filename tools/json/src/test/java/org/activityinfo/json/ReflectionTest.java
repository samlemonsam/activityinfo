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
