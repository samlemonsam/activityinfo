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

import java.util.ArrayList;
import java.util.List;

public class FormRecordUpdate {
  private boolean deleted;

  private JsonValue fieldValues;

  public FormRecordUpdate() {
  }

  public boolean isDeleted() {
    return deleted;
  }

  public JsonValue getFieldValues() {
    return fieldValues;
  }

  public static FormRecordUpdate fromJson(JsonValue jsonElement) {
      JsonValue jsonObject = jsonElement;
    FormRecordUpdate model = new FormRecordUpdate();
    model.deleted = jsonObject.get("deleted").asBoolean();
      model.fieldValues = jsonObject.get("fieldValues");
    return model;
  }

  public static List<FormRecordUpdate> fromJsonArray(JsonValue jsonArray) {
    List<FormRecordUpdate> list = new ArrayList<FormRecordUpdate>();
    for(JsonValue element : jsonArray.values()) {
      list.add(fromJson(element));
    }
    return list;
  }
}
