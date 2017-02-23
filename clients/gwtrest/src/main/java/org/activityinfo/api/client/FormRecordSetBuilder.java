package org.activityinfo.api.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.activityinfo.model.form.FormRecord;

public class FormRecordSetBuilder {
  private JsonObject jsonObject = new JsonObject();

  private JsonArray records = new JsonArray();

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
    this.jsonObject.add("formId", new JsonPrimitive(formId));
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
