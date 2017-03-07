package org.activityinfo.api.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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

  public static FormRecordUpdate fromJson(JsonElement jsonElement) {
    JsonObject jsonObject = jsonElement.getAsJsonObject();
    FormRecordUpdate model = new FormRecordUpdate();
    model.deleted = jsonObject.get("deleted").getAsBoolean();
    model.fieldValues = jsonObject.get("fieldValues").getAsJsonObject();
    return model;
  }

  public static List<FormRecordUpdate> fromJsonArray(JsonArray jsonArray) {
    List<FormRecordUpdate> list = new ArrayList<FormRecordUpdate>();
    for(JsonElement element : jsonArray) {
      list.add(fromJson(element));
    }
    return list;
  }
}
