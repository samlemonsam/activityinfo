/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.activityinfo.json;

import com.google.gwt.dev.protobuf.DynamicMessage;
import com.google.gwt.junit.client.GWTTestCase;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class JsonUtilGwtTest extends GWTTestCase {

  @Override
  public String getModuleName() {
    return "org.activityinfo.Json";
  }

  public void testCoercions() {
    // test boolean coercions
    JsonBoolean boolTrue = Json.create(true);
    JsonBoolean boolFalse = Json.create(false);
    // true -> 1, false -> 0
    assertEquals(true, boolTrue.asBoolean());
    assertEquals(false, boolFalse.asBoolean());

    JsonString trueString = Json.create("true");
    JsonString falseString = Json.create("");
    // "" -> false, others true
    assertEquals(true, trueString.asBoolean());
    assertEquals(false, falseString.asBoolean());

    // != 0 -> true, otherwise if 0.0 or -0.0 false
    JsonNumber trueNumber = Json.create(1.0);
    JsonNumber falseNumber = Json.create(0.0);
    JsonNumber falseNumber2 = Json.create(-0.0);
    assertEquals(true, trueNumber.asBoolean());
    assertEquals(false, falseNumber.asBoolean());
    assertEquals(false, falseNumber2.asBoolean());

    // array or object is true
    assertEquals(true, Json.createArray().asBoolean());
    assertEquals(true, Json.createObject().asBoolean());

    // null is false
    assertEquals(false, Json.createNull().asBoolean());

    // test number coercions
    assertEquals(1.0, boolTrue.asNumber());
    assertEquals(0.0, boolFalse.asNumber());

    assertEquals(42.0, Json.create("42").asNumber());
    // non numbers are NaN
    assertTrue(Double.isNaN(trueString.asNumber()));
    // null is 0
    assertEquals(0.0, Json.createNull().asNumber());
    // "" is 0
    assertEquals(0.0, falseString.asNumber());

    // [] -> 0
    assertEquals(0.0, Json.createArray().asNumber());
    // [[42]] -> 42
    JsonArray nested = Json.createArray();
    JsonArray outer = Json.createArray();
    outer.set(0, nested);
    nested.set(0, 42);
    assertEquals(42.0, outer.asNumber());

    // [[42, 45]] -> NaN
    nested.set(1, 45);
    assertTrue(Double.isNaN(outer.asNumber()));

    // object -> NaN
    assertTrue(Double.isNaN(Json.createObject().asNumber()));


    // test string coercions
    assertEquals("true", boolTrue.asString());
    assertEquals("false", boolFalse.asString());
    assertEquals("true", trueString.asString());

    assertTrue(Json.createNull().asString() == null);
    assertEquals("42", Json.create(42).asString());

    // [[42, 45], [52, 55]] -> "42, 45, 52, 55"
    JsonArray inner2 = Json.createArray();
    inner2.set(0, 52);
    inner2.set(1, 55);
    outer.set(1, inner2);
    assertEquals("42,45,52,55", outer.asString());

    // object -> [object Object]
    assertEquals("[object Object]", Json.createObject().asString());
  }

  public void testBooleanTypes() {
    JsonValue booleanValue = Json.create(true);
    assertEquals(JsonType.BOOLEAN, booleanValue.getType());

    JsonValue doubleValue = Json.create(1.5);
    assertEquals(JsonType.NUMBER, doubleValue.getType());
  }


  public void testIllegalParse() {
    try {
      Json.parse("{ \"a\": new String() }");
      fail("Expected JsonException to be thrown");
    } catch (JsonException je) {
      // Expected
    }
  }

  public void testLegalParse() {
    JsonValue obj = Json.parse(
        "{ \"a\":1, \"b\":\"hello\", \"c\": true,"
            + "\"d\": null, \"e\": [1,2,3,4], \"f\": {} }");
    assertNotNull(obj);
  }

  public void testNative() {
    JsonObject obj = Json.createObject();
    obj.put("x", 42);
    Object nativeObj = obj.toNative();

    JsonObject result = nativeMethod(nativeObj);
    assertEquals(43.0, result.get("y").asNumber());
  }


  public void testStringify() {
    String json = "{\"a\":1,\"b\":\"hello\",\"c\":true,"
        + "\"d\":null,\"e\":[1,2,3,4],\"f\":{\"x\":1}}";
    assertEquals(json, Json.stringify(Json.parse(json)));
  }

  public void testStringifyCycle() {
    String json = "{\"a\":1,\"b\":\"hello\",\"c\":true,"
        + "\"d\":null,\"e\":[1,2,3,4],\"f\":{\"x\":1}}";
    JsonObject obj = Json.parse(json).getAsJsonObject();
    obj.put("cycle", obj);
    try {
      Json.stringify(obj);
      fail("Expected JsonException for object cycle");
    } catch (Exception je) {
    }
  }


  public void testStringifyNonCycle() {
    String json = "{\"a\":1,\"b\":\"hello\",\"c\":true,"
        + "\"d\":null,\"e\":[1,2,3,4],\"f\":{\"x\":1}}";
    JsonObject obj = Json.parse(json).getAsJsonObject();
    JsonObject obj2 = Json.parse("{\"x\": 1, \"y\":2}").getAsJsonObject();
    obj.put("nocycle", obj2);
    obj.put("nocycle2", obj2);
    try {
      Json.stringify(obj);
    } catch (JsonException je) {
      fail("JsonException for object cycle when none exists: " + je);
    }
  }

//  public void testStringifyOrder() {
//
//    JsonObject obj = Json.instance().createObject();
//    obj.put("x", "hello");
//    obj.put("a", "world");
//    obj.put("2", 21);
//    obj.put("1", 42);
//    // numbers come first, in ascending order, non-numbers in order of assignment
//    assertEquals("{\"1\":42,\"2\":21,\"x\":\"hello\",\"a\":\"world\"}",
//        obj.toJson());
//  }
//
//  public void testStringifySkipKeys() {
//    String expectedJson = "{\"a\":1,\"b\":\"hello\",\"c\":true,"
//        + "\"d\":null,\"e\":[1,2,3,4],\"f\":{\"x\":1}}";
//    String json = "{\"a\":1,\"b\":\"hello\",\"c\":true,"
//        + "\"$H\": 1,"
//        + "\"__gwt_ObjectId\": 1,"
//        + "\"d\":null,\"e\":[1,2,3,4],\"f\":{\"x\":1}}";
//    assertEquals(expectedJson, Json.stringify(
//        JsonUtil.parse(json)));
//  }
//
  public void testStringifyDoubleNanInfinity() {
    JsonNumber json = Json.create(Double.NaN);
    assertEquals("null",Json.stringify(json));
    json = Json.create(Double.POSITIVE_INFINITY);
    assertEquals("null",Json.stringify(json));
    json = Json.create(Double.NEGATIVE_INFINITY);
    assertEquals("null",Json.stringify(json));
  }
//
//  public void testJsonNumberToJsonDoubleNanInfinity() {
//    JsonNumber json = Json.create(Double.NaN);
//    assertEquals("null",json.toJson());
//    json = Json.create(Double.POSITIVE_INFINITY);
//    assertEquals("null",json.toJson());
//    json = Json.create(Double.NEGATIVE_INFINITY);
//    assertEquals("null",json.toJson());
//  }

  private native JsonObject nativeMethod(Object o) /*-{
    o.y = o.x + 1;
    return o;
  }-*/;


  public void testSetStringNull() {
    JsonObject object = Json.createObject();
    object.put("foo", (String)null);

    assertTrue(object.get("foo").isJsonNull());
  }

  public void testObjectNullValues() {

    JsonObject object = Json.createObject();
    object.put("a", (String)null);
    object.put("b", Json.createNull());

    assertTrue(object.get("a").isJsonNull());
    assertTrue(object.get("b").isJsonNull());
    assertTrue(object.get("c") == null);
  }

  public void testArrayParse() {

    JsonValue jsonValue = Json.parse("[1,2,3,4]");
    JsonArray jsonArray = jsonValue.getAsJsonArray();

    int count = 0;
    double sum = 0;
    for (JsonValue value : jsonArray.values()) {
      count++;
      sum += value.asNumber();
    }
    assertEquals(4, count);
    assertEquals(1d+2d+3d+4d, sum);
  }


  public void testStringArrayParse() {

    JsonValue jsonValue = Json.parse("[\"a\", \"b\"]");
    JsonArray jsonArray = jsonValue.getAsJsonArray();

    String concat = "";
    for (JsonValue value : jsonArray.values()) {
      concat += value.asString();
    }
    assertEquals("ab", concat);
  }





// Will only work on GWT 2.8+
//  public void testJsObjects() throws JsonMappingException {
//
//    DummyObject o = new DummyObject();
//    o.d = 41;
//    o.s = "Hello World";
//    o.s0 = null;
//    o.i = 99;
//    o.object = Json.createObject();
//    o.object.put("a", "Brave New World");
//
//    JsonObject jo = Json.toJson(o).getAsJsonObject();
//
//    assertEquals(41d, jo.getNumber("d"));
//    assertEquals("Hello World", jo.getString("s"));
//    assertNull(jo.getString("s0"));
//    assertEquals(99d, jo.getNumber("i"));
//    assertEquals(42d, jo.getNumber("privateField"));
//
//    JsonObject joo = jo.getObject("object");
//    assertEquals("Brave New World", joo.getString("a"));
//
//    // Now back to a Java object
//
//    DummyObject o2 = Json.fromJson(DummyObject.class, jo);
//
//    assertEquals(o.d, o2.d);
//    assertEquals(o.s, o2.s);
//    assertEquals(o.s0, o2.s0);
//    assertEquals(o.i, o2.i);
//
//  }

}