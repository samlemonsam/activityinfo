package org.activityinfo.api.client;

import org.activityinfo.json.JsonArray;
import org.activityinfo.json.JsonObject;
import org.activityinfo.json.JsonValue;

import java.util.ArrayList;
import java.util.List;

public class FormRecordUpdate {
  private boolean deleted;

  private JsonObject fieldValues;

  public FormRecordUpdate() {
  }

  public boolean isDeleted() {
    return deleted;
  }

  public JsonObject getFieldValues() {
    return fieldValues;
  }

  public static FormRecordUpdate fromJson(JsonValue jsonElement) {
    JsonObject jsonObject = jsonElement.getAsJsonObject();
    FormRecordUpdate model = new FormRecordUpdate();
    model.deleted = jsonObject.get("deleted").asBoolean();
    model.fieldValues = jsonObject.get("fieldValues").getAsJsonObject();
    return model;
  }

  public static List<FormRecordUpdate> fromJsonArray(JsonArray jsonArray) {
    List<FormRecordUpdate> list = new ArrayList<FormRecordUpdate>();
    for(JsonValue element : jsonArray.values()) {
      list.add(fromJson(element));
    }
    return list;
  }
}
