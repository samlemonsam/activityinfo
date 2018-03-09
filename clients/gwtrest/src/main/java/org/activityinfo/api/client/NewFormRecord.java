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

import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.JsonParsing;

import java.util.ArrayList;
import java.util.List;

public class NewFormRecord {
  private String id;

  private String parentRecordId;

  private String keyId;

  private JsonValue fieldValues;

  public NewFormRecord() {
  }

  public String getId() {
    return id;
  }

  public String getParentRecordId() {
    return parentRecordId;
  }

  public String getKeyId() {
    return keyId;
  }

  public JsonValue getFieldValues() {
    return fieldValues;
  }

  public static NewFormRecord fromJson(JsonValue jsonElement) {
      JsonValue jsonObject = jsonElement;
    NewFormRecord model = new NewFormRecord();
    model.id = JsonParsing.toNullableString(jsonObject.get("id"));
    model.parentRecordId = JsonParsing.toNullableString(jsonObject.get("parentRecordId"));
    model.keyId = JsonParsing.toNullableString(jsonObject.get("keyId"));
      model.fieldValues = jsonObject.get("fieldValues");
    return model;
  }

  public static List<NewFormRecord> fromJsonArray(JsonValue jsonArray) {
    List<NewFormRecord> list = new ArrayList<NewFormRecord>();
    for(JsonValue element : jsonArray.values()) {
      list.add(fromJson(element));
    }
    return list;
  }
}
