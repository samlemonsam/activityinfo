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
