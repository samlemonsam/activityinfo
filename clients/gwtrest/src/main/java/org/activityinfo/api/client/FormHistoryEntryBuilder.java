package org.activityinfo.api.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class FormHistoryEntryBuilder {
  private JsonObject jsonObject = new JsonObject();

  private JsonArray values = new JsonArray();

  public FormHistoryEntryBuilder() {
    jsonObject.add("values", values);
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
   * @param formId id of the form
   */
  public FormHistoryEntryBuilder setFormId(String formId) {
    this.jsonObject.add("formId", new JsonPrimitive(formId));
    return this;
  }

  /**
   * Sets the recordId.
   *
   * @param recordId id of the record
   */
  public FormHistoryEntryBuilder setRecordId(String recordId) {
    this.jsonObject.add("recordId", new JsonPrimitive(recordId));
    return this;
  }

  /**
   * Sets the time.
   *
   * @param time the time, in seconds since 1970-01-01, that the change was made
   */
  public FormHistoryEntryBuilder setTime(int time) {
    this.jsonObject.add("time", new JsonPrimitive(time));
    return this;
  }

  /**
   * Sets the subFieldId.
   *
   * @param subFieldId for sub records, the subForm field to which this sub record belongs
   */
  public FormHistoryEntryBuilder setSubFieldId(String subFieldId) {
    this.jsonObject.add("subFieldId", new JsonPrimitive(subFieldId));
    return this;
  }

  /**
   * Sets the subFieldLabel.
   *
   * @param subFieldLabel for sub records, the label of the subForm field to which this sub record belongs
   */
  public FormHistoryEntryBuilder setSubFieldLabel(String subFieldLabel) {
    this.jsonObject.add("subFieldLabel", new JsonPrimitive(subFieldLabel));
    return this;
  }

  /**
   * Sets the subRecordKey.
   *
   * @param subRecordKey for keyed sub forms, such as monthly, weekly, or daily subForms, this is a human readable label describing the key, for example '2016-06'
   */
  public FormHistoryEntryBuilder setSubRecordKey(String subRecordKey) {
    this.jsonObject.add("subRecordKey", new JsonPrimitive(subRecordKey));
    return this;
  }

  /**
   * Sets the changeType.
   *
   */
  public FormHistoryEntryBuilder setChangeType(String changeType) {
    this.jsonObject.add("changeType", new JsonPrimitive(changeType));
    return this;
  }

  /**
   * Sets the userName.
   *
   * @param userName the name of the user who made the change
   */
  public FormHistoryEntryBuilder setUserName(String userName) {
    this.jsonObject.add("userName", new JsonPrimitive(userName));
    return this;
  }

  /**
   * Sets the userEmail.
   *
   * @param userEmail the email address of the user who made the change
   */
  public FormHistoryEntryBuilder setUserEmail(String userEmail) {
    this.jsonObject.add("userEmail", new JsonPrimitive(userEmail));
    return this;
  }

  /**
   * Adds a value.
   *
   */
  public FormHistoryEntryBuilder addValue(FormValueChangeBuilder value) {
    values.add(value.toJsonObject());
    return this;
  }
}
