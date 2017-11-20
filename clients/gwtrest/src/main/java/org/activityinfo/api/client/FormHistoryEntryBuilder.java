package org.activityinfo.api.client;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;

public class FormHistoryEntryBuilder {
  private JsonValue jsonObject = Json.createObject();

  private JsonValue values = Json.createArray();

  public FormHistoryEntryBuilder() {
    jsonObject.add("values", values);
  }

  public String toJsonString() {
    return jsonObject.toJson();
  }

  public JsonValue toJsonObject() {
    return jsonObject;
  }

  /**
   * Sets the formId.
   *
   * @param formId id of the form
   */
  public FormHistoryEntryBuilder setFormId(String formId) {
    this.jsonObject.put("formId", formId);
    return this;
  }

  /**
   * Sets the recordId.
   *
   * @param recordId id of the record
   */
  public FormHistoryEntryBuilder setRecordId(String recordId) {
    this.jsonObject.put("recordId", recordId);
    return this;
  }

  /**
   * Sets the time.
   *
   * @param time the time, in seconds since 1970-01-01, that the change was made
   */
  public FormHistoryEntryBuilder setTime(int time) {
    this.jsonObject.put("time", time);
    return this;
  }

  /**
   * Sets the subFieldId.
   *
   * @param subFieldId for sub records, the subForm field to which this sub record belongs
   */
  public FormHistoryEntryBuilder setSubFieldId(String subFieldId) {
    this.jsonObject.put("subFieldId", subFieldId);
    return this;
  }

  /**
   * Sets the subFieldLabel.
   *
   * @param subFieldLabel for sub records, the label of the subForm field to which this sub record belongs
   */
  public FormHistoryEntryBuilder setSubFieldLabel(String subFieldLabel) {
    this.jsonObject.put("subFieldLabel", subFieldLabel);
    return this;
  }

  /**
   * Sets the subRecordKey.
   *
   * @param subRecordKey for keyed sub forms, such as monthly, weekly, or daily subForms, this is a human readable label describing the key, for example '2016-06'
   */
  public FormHistoryEntryBuilder setSubRecordKey(String subRecordKey) {
    this.jsonObject.put("subRecordKey", subRecordKey);
    return this;
  }

  /**
   * Sets the changeType.
   *
   */
  public FormHistoryEntryBuilder setChangeType(String changeType) {
    this.jsonObject.put("changeType", changeType);
    return this;
  }

  /**
   * Sets the userName.
   *
   * @param userName the name of the user who made the change
   */
  public FormHistoryEntryBuilder setUserName(String userName) {
    this.jsonObject.put("userName", userName);
    return this;
  }

  /**
   * Sets the userEmail.
   *
   * @param userEmail the email address of the user who made the change
   */
  public FormHistoryEntryBuilder setUserEmail(String userEmail) {
    this.jsonObject.put("userEmail", userEmail);
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
