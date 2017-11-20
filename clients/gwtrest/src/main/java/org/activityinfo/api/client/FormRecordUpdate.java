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
