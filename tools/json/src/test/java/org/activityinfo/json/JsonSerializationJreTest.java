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

import com.google.common.collect.Lists;
import junit.framework.TestCase;
import org.activityinfo.json.impl.JsonUtil;

import java.io.*;
import java.util.List;

public class JsonSerializationJreTest extends TestCase {

  public void testSerializeNull() throws Exception {
    JsonValue null1 = Json.createNull();
    JsonValue null2 = Json.createNull();
    JsonValue out = serializeDeserialize(null1);
    assertJsonEquals(null1, out);
    assertSame(null1, out);
    assertSame(null2, out);
    assertSame(Json.createNull(), out);
  }

  public void testSerializeObject() throws Exception {
    JsonValue foo = Json.createObject();
    foo.put("true", true);
    foo.put("string", "string");
    foo.put("number", 1.25);

    JsonValue subObject = Json.createObject();
    subObject.put("false", false);
    subObject.put("string2", "string2");
    subObject.put("number", -151);

    JsonValue subArray = Json.createArray();
    subArray.set(0, true);
    subArray.set(1, 1);
    subArray.set(2, "2");

    foo.put("object", subObject);
    foo.put("array", subArray);
    foo.put("null", Json.createNull());

    assertJsonEqualsAfterSerialization(foo);
  }

  public void testSerializeArray() throws Exception {
    JsonValue subObject = Json.createObject();
    subObject.put("false", false);
    subObject.put("string2", "string2");
    subObject.put("number", -151);

    JsonValue subArray = Json.createArray();
    subArray.set(0, true);
    subArray.set(1, 1);
    subArray.set(2, "2");

    JsonValue array = Json.createArray();
    array.set(0, true);
    array.set(1, false);
    array.set(2, 2);
    array.set(3, "3");
    array.set(4, subObject);
    array.set(5, subArray);

    assertJsonEqualsAfterSerialization(array);
  }

  public void testSerializeBoolean() throws Exception {
    assertJsonEqualsAfterSerialization(Json.create(true));
    assertJsonEqualsAfterSerialization(Json.create(false));
  }

  public void testSerializeString() throws Exception {
    assertJsonEqualsAfterSerialization(Json.create("foo"));
    assertJsonEqualsAfterSerialization(Json.create(""));
  }

  public void testSerializeNumber() throws Exception {
    assertJsonEqualsAfterSerialization(Json.create(0));
    assertJsonEqualsAfterSerialization(Json.create(-1.213123123));
  }

  public void testSerializeEnumList() {
    List<DummyEnum> colors = Lists.newArrayList(DummyEnum.BLUE, DummyEnum.BLUE, DummyEnum.RED);
    JsonArrays.toJsonArrayFromEnums(colors);

  }

  private <T extends Serializable & JsonValue> void assertJsonEqualsAfterSerialization(
      T in) throws Exception {
    T out = serializeDeserialize(in);
    assertNotSame(in, out);
    assertJsonEquals(in, out);
  }

  private void assertJsonEquals(JsonValue a, JsonValue b) {
    assertEquals(a.toJson(), b.toJson());
  }

  @SuppressWarnings("unchecked")
  public <T extends Serializable & JsonValue> T serializeDeserialize(
      T originalJsonValue) throws Exception {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(buffer);
    out.writeObject(originalJsonValue);
    out.close();

    ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(
        buffer.toByteArray()));
    T processedJsonValue = (T) in.readObject();
    in.close();
    return processedJsonValue;
  }

  public void testQuoteCharacters() {
    // See spec at https://tools.ietf.org/html/rfc7159
    for (int i = 0; i < 0xffff; i++) {
      String unencodedString = String.valueOf((char) i);
      String res = JsonUtil.quote(unencodedString);
      if (res.equals("\"" + unencodedString + "\"")) {
        // passed through unescaped
        if (i == 0x20 || i == 0x21 || (i >= 0x23 && i <= 0x5b)
                || i >= 0x5d) {
          // ok for %x20-21 / %x23-5B / %x5D-10FFFF
        } else {
          fail("Character " + i + " must be escaped in JSON");
        }
      } else {
        // Was escaped, should be in format \\X or \\uXXXX
        if (res.length() == 4) {
          // "\\X"
          char escapedChar = res.charAt(2);
          // btnfr\"
          if (escapedChar == 'b') {
            assertEquals('\b',i);
          } else if (escapedChar == 't') {
            assertEquals('\t',i);
          } else if (escapedChar == 'n') {
            assertEquals('\n',i);
          } else if (escapedChar == 'f') {
            assertEquals('\f',i);
          } else if (escapedChar == 'r') {
            assertEquals('\r',i);
          } else if (escapedChar == '"') {
            assertEquals('"',i);
          } else if (escapedChar == '\\') {
            assertEquals('\\',i);
          } else {
            fail("Character" + i + " was unexpectedly escaped as "+escapedChar);
          }
        } else {
          assertTrue("Character " + i + " was incorrectly encoded as " +
                  res,res.matches("\"\\\\u....\""));
        }
      }
    }
  }

  public void testEscapeControlChars() {
    String unicodeString = "\u2060Test\ufeffis a test\u17b5";
    assertEquals("\\u2060Test\\ufeffis a test\\u17b5",
            JsonUtil.escapeControlChars(unicodeString));
  }
}
