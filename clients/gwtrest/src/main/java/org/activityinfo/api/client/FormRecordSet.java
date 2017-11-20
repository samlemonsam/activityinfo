package org.activityinfo.api.client;

import org.activityinfo.json.JsonValue;
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

  public static FormRecordSet fromJson(JsonValue jsonElement) {
      JsonValue jsonObject = jsonElement;
    FormRecordSet model = new FormRecordSet();
    model.formId = JsonParsing.toNullableString(jsonObject.get("formId"));
    model.records = FormRecord.fromJsonArray(jsonObject.get("records"));
    return model;
  }

  public static List<FormRecordSet> fromJsonArray(JsonValue jsonArray) {
    List<FormRecordSet> list = new ArrayList<FormRecordSet>();
    for(JsonValue element : jsonArray.values()) {
      list.add(fromJson(element));
    }
    return list;
  }
}
