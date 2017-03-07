package org.activityinfo.api.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.model.form.JsonParsing;

import java.util.ArrayList;
import java.util.List;

public class NewFormRecord {
  private String id;

  private String parentRecordId;

  private String keyId;

  private JsonObject fieldValues;

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

  public JsonObject getFieldValues() {
    return fieldValues;
  }

  public static NewFormRecord fromJson(JsonElement jsonElement) {
    JsonObject jsonObject = jsonElement.getAsJsonObject();
    NewFormRecord model = new NewFormRecord();
    model.id = JsonParsing.toNullableString(jsonObject.get("id"));
    model.parentRecordId = JsonParsing.toNullableString(jsonObject.get("parentRecordId"));
    model.keyId = JsonParsing.toNullableString(jsonObject.get("keyId"));
    model.fieldValues = jsonObject.get("fieldValues").getAsJsonObject();
    return model;
  }

  public static List<NewFormRecord> fromJsonArray(JsonArray jsonArray) {
    List<NewFormRecord> list = new ArrayList<NewFormRecord>();
    for(JsonElement element : jsonArray) {
      list.add(fromJson(element));
    }
    return list;
  }
}
