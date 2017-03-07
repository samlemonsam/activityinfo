package org.activityinfo.api.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class FormRecordUpdateBuilder {
  private JsonObject jsonObject = new JsonObject();

  private JsonObject fieldValues = new JsonObject();

  public FormRecordUpdateBuilder() {
    jsonObject.add("fieldValues", fieldValues);
  }

  public String toJsonString() {
    return jsonObject.toString();
  }

  public JsonObject toJsonObject() {
    return jsonObject;
  }

  /**
   * Sets the deleted.
   *
   * @param deleted True if the record should be deleted
   */
  public FormRecordUpdateBuilder setDeleted(boolean deleted) {
    this.jsonObject.add("deleted", new JsonPrimitive(deleted));
    return this;
  }

  public FormRecordUpdateBuilder setFieldValue(String name, String value) {
    fieldValues.addProperty(name, value);
    return this;
  }

  public FormRecordUpdateBuilder setFieldValue(String name, Number value) {
    fieldValues.addProperty(name, value);
    return this;
  }

  public FormRecordUpdateBuilder setFieldValue(String name, boolean value) {
    fieldValues.addProperty(name, value);
    return this;
  }

  public FormRecordUpdateBuilder setFieldValue(String name, JsonElement value) {
    fieldValues.add(name, value);
    return this;
  }
}
