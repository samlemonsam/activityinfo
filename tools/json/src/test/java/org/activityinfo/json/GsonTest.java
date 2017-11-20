package org.activityinfo.json;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class GsonTest {

    @Test
    public void testSetStringNull() {
        com.google.gson.JsonObject object = new com.google.gson.JsonObject();
        object.addProperty("foo", (String)null);

        assertTrue(object.get("foo").isJsonNull());

    }

}
