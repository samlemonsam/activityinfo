package org.activityinfo.api.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class NewFormRecordBuilder {
  private JsonObject jsonObject = new JsonObject();

  private JsonObject fieldValues = new JsonObject();

  public NewFormRecordBuilder() {
    jsonObject.add("fieldValues", fieldValues);
  }

  public String toJsonString() {
    return jsonObject.toString();
  }

  public JsonObject toJsonObject() {
    return jsonObject;
  }

  /**
   * Sets the id.
   *
   * @param id client-generated id
   */
  public NewFormRecordBuilder setId(String id) {
    this.jsonObject.add("id", new JsonPrimitive(id));
    return this;
  }

  /**
   * Sets the parentRecordId.
   *
   * @param parentRecordId id of the parent FormRecord, if this is record is a member of a subform
   */
  public NewFormRecordBuilder setParentRecordId(String parentRecordId) {
    this.jsonObject.add("parentRecordId", new JsonPrimitive(parentRecordId));
    return this;
  }

  /**
   * Sets the keyId.
   *
   * @param keyId key id, if this is record is a member of a subform
   */
  public NewFormRecordBuilder setKeyId(String keyId) {
    this.jsonObject.add("keyId", new JsonPrimitive(keyId));
    return this;
  }

  public NewFormRecordBuilder setFieldValue(String name, String value) {
    fieldValues.addProperty(name, value);
    return this;
  }

  public NewFormRecordBuilder setFieldValue(String name, Number value) {
    fieldValues.addProperty(name, value);
    return this;
  }

  public NewFormRecordBuilder setFieldValue(String name, boolean value) {
    fieldValues.addProperty(name, value);
    return this;
  }

  public NewFormRecordBuilder setFieldValue(String name, JsonElement value) {
    fieldValues.add(name, value);
    return this;
  }
}
