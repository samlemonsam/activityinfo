package org.activityinfo.api.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.model.form.JsonParsing;

import java.util.ArrayList;
import java.util.List;

public class FormValueChange {
  private String fieldId;

  private String fieldLabel;

  private String oldValueLabel;

  private String newValueLabel;

  private String subFormKind;

  private String subFormKey;

  public FormValueChange() {
  }

  public String getFieldId() {
    return fieldId;
  }

  public String getFieldLabel() {
    return fieldLabel;
  }

  public String getOldValueLabel() {
    return oldValueLabel;
  }

  public String getNewValueLabel() {
    return newValueLabel;
  }

  public String getSubFormKind() {
    return subFormKind;
  }

  public String getSubFormKey() {
    return subFormKey;
  }

  public static FormValueChange fromJson(JsonElement jsonElement) {
    JsonObject jsonObject = jsonElement.getAsJsonObject();
    FormValueChange model = new FormValueChange();
    model.fieldId = JsonParsing.toNullableString(jsonObject.get("fieldId"));
    model.fieldLabel = JsonParsing.toNullableString(jsonObject.get("fieldLabel"));
    model.oldValueLabel = JsonParsing.toNullableString(jsonObject.get("oldValueLabel"));
    model.newValueLabel = JsonParsing.toNullableString(jsonObject.get("newValueLabel"));
    model.subFormKind = JsonParsing.toNullableString(jsonObject.get("subFormKind"));
    model.subFormKey = JsonParsing.toNullableString(jsonObject.get("subFormKey"));
    return model;
  }

  public static List<FormValueChange> fromJsonArray(JsonArray jsonArray) {
    List<FormValueChange> list = new ArrayList<FormValueChange>();
    for(JsonElement element : jsonArray) {
      list.add(fromJson(element));
    }
    return list;
  }
}
