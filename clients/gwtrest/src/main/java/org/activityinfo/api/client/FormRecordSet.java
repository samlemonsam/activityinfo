package org.activityinfo.api.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.form.JsonParsing;

import java.util.ArrayList;
import java.util.List;

public class FormRecordSet {
  private String formId;

  private List<FormRecord> records;

  public FormRecordSet() {
  }

  public FormRecordSet(List<FormRecord> records) {
    this.records = records;
  }

  public String getFormId() {
    return formId;
  }

  public List<FormRecord> getRecords() {
    return records;
  }

  public static FormRecordSet fromJson(JsonElement jsonElement) {
    JsonObject jsonObject = jsonElement.getAsJsonObject();
    FormRecordSet model = new FormRecordSet();
    model.formId = JsonParsing.toNullableString(jsonObject.get("formId"));
    model.records = FormRecord.fromJsonArray(jsonObject.get("records").getAsJsonArray());
    return model;
  }

  public static List<FormRecordSet> fromJsonArray(JsonArray jsonArray) {
    List<FormRecordSet> list = new ArrayList<FormRecordSet>();
    for(JsonElement element : jsonArray) {
      list.add(fromJson(element));
    }
    return list;
  }
}
