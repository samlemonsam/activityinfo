package org.activityinfo.api.client;

import org.activityinfo.json.JsonArray;
import org.activityinfo.json.JsonObject;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.JsonParsing;

import java.util.ArrayList;
import java.util.List;

public class FormHistoryEntry {
  private String formId;

  private String recordId;

  private int time;

  private String subFieldId;

  private String subFieldLabel;

  private String subRecordKey;

  private String changeType;

  private String userName;

  private String userEmail;

  private List<FormValueChange> values;

  public FormHistoryEntry() {
  }

  public String getFormId() {
    return formId;
  }

  public String getRecordId() {
    return recordId;
  }

  public int getTime() {
    return time;
  }

  public String getSubFieldId() {
    return subFieldId;
  }

  public String getSubFieldLabel() {
    return subFieldLabel;
  }

  public String getSubRecordKey() {
    return subRecordKey;
  }

  public String getChangeType() {
    return changeType;
  }

  public String getUserName() {
    return userName;
  }

  public String getUserEmail() {
    return userEmail;
  }

  public List<FormValueChange> getValues() {
    return values;
  }

  public static FormHistoryEntry fromJson(JsonValue jsonElement) {
    JsonObject jsonObject = jsonElement.getAsJsonObject();
    FormHistoryEntry model = new FormHistoryEntry();
    model.formId = JsonParsing.toNullableString(jsonObject.get("formId"));
    model.recordId = JsonParsing.toNullableString(jsonObject.get("recordId"));
    model.time = jsonObject.get("time").asInt();
    model.subFieldId = JsonParsing.toNullableString(jsonObject.get("subFieldId"));
    model.subFieldLabel = JsonParsing.toNullableString(jsonObject.get("subFieldLabel"));
    model.subRecordKey = JsonParsing.toNullableString(jsonObject.get("subRecordKey"));
    model.changeType = JsonParsing.toNullableString(jsonObject.get("changeType"));
    model.userName = JsonParsing.toNullableString(jsonObject.get("userName"));
    model.userEmail = JsonParsing.toNullableString(jsonObject.get("userEmail"));
    model.values = FormValueChange.fromJsonArray(jsonObject.get("values").getAsJsonArray());
    return model;
  }

  public static List<FormHistoryEntry> fromJsonArray(JsonArray jsonArray) {
    List<FormHistoryEntry> list = new ArrayList<FormHistoryEntry>();
    for(JsonValue element : jsonArray.values()) {
      list.add(fromJson(element));
    }
    return list;
  }
}
