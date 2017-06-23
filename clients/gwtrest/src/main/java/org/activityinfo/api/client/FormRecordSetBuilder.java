package org.activityinfo.api.client;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonArray;
import org.activityinfo.json.JsonObject;
import org.activityinfo.model.form.FormRecord;

public class FormRecordSetBuilder {
  private JsonObject jsonObject = Json.createObject();

  private JsonArray records = Json.createArray();

  public FormRecordSetBuilder() {
    jsonObject.add("records", records);
  }

  public String toJsonString() {
    return jsonObject.toString();
  }

  public JsonObject toJsonObject() {
    return jsonObject;
  }

  /**
   * Sets the formId.
   *
   * @param formId The id of the form to which this record belongs
   */
  public FormRecordSetBuilder setFormId(String formId) {
    this.jsonObject.put("formId", formId);
    return this;
  }

  /**
   * Adds a record.
   *
   */
  public FormRecordSetBuilder addRecord(FormRecord value) {
    records.add(value.toJsonElement());
    return this;
  }
}
