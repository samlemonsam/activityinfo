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
package org.activityinfo.api.client;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;

public class NewFormRecordBuilder {
  private JsonValue jsonObject = Json.createObject();

  private JsonValue fieldValues = Json.createObject();

  public NewFormRecordBuilder() {
    jsonObject.add("fieldValues", fieldValues);
  }

  public String toJsonString() {
    return jsonObject.toJson();
  }

  public JsonValue toJsonObject() {
    return jsonObject;
  }

  /**
   * Sets the id.
   *
   * @param id client-generated id
   */
  public NewFormRecordBuilder setId(String id) {
    this.jsonObject.put("id", id);
    return this;
  }

  /**
   * Sets the parentRecordId.
   *
   * @param parentRecordId id of the parent FormRecord, if this is record is a member of a subform
   */
  public NewFormRecordBuilder setParentRecordId(String parentRecordId) {
    this.jsonObject.put("parentRecordId", parentRecordId);
    return this;
  }

  /**
   * Sets the keyId.
   *
   * @param keyId key id, if this is record is a member of a subform
   */
  public NewFormRecordBuilder setKeyId(String keyId) {
    this.jsonObject.put("keyId", keyId);
    return this;
  }

  public NewFormRecordBuilder setFieldValue(String name, String value) {
    fieldValues.put(name, value);
    return this;
  }

  public NewFormRecordBuilder setFieldValue(String name, Number value) {
    fieldValues.put(name, value.doubleValue());
    return this;
  }

  public NewFormRecordBuilder setFieldValue(String name, boolean value) {
    fieldValues.put(name, value);
    return this;
  }

  public NewFormRecordBuilder setFieldValue(String name, JsonValue value) {
    fieldValues.add(name, value);
    return this;
  }
}
