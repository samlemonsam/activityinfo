package org.activityinfo.api.client;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonObject;
import org.activityinfo.json.JsonValue;

public class NewFormRecordBuilder {
  private JsonObject jsonObject = Json.createObject();

  private JsonObject fieldValues = Json.createObject();

  public NewFormRecordBuilder() {
    jsonObject.add("fieldValues", fieldValues);
  }

  public String toJsonString() {
    return jsonObject.toJson();
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
    this.jsonObject.put("id", id);
    return this;
  }

  /**
   * Sets the parentRecordId.
   *
   * @param parentRecordId id of the parent FormRecord, if this is record is a member of a subform
   */
  public NewFormRecordBuilder setParentRecordId(String parentRecordId) {
    this.jsonObject.put("parentRecordId", parentRecordId);
    return this;
  }

  /**
   * Sets the keyId.
   *
   * @param keyId key id, if this is record is a member of a subform
   */
  public NewFormRecordBuilder setKeyId(String keyId) {
    this.jsonObject.put("keyId", keyId);
    return this;
  }

  public NewFormRecordBuilder setFieldValue(String name, String value) {
    fieldValues.put(name, value);
    return this;
  }

  public NewFormRecordBuilder setFieldValue(String name, Number value) {
    fieldValues.put(name, value.doubleValue());
    return this;
  }

  public NewFormRecordBuilder setFieldValue(String name, boolean value) {
    fieldValues.put(name, value);
    return this;
  }

  public NewFormRecordBuilder setFieldValue(String name, JsonValue value) {
    fieldValues.add(name, value);
    return this;
  }
}
