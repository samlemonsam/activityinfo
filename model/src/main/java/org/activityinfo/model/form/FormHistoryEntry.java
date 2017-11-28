package org.activityinfo.model.form;

import org.activityinfo.json.JsonValue;

import java.util.ArrayList;
import java.util.List;

public class FormHistoryEntry {
  private String formId;

  private String recordId;

  private int time;

  private String subFieldId;

  private String subFieldLabel;

  private String subRecordKey;

  private String changeType;

  private String userName;

  private String userEmail;

  private List<FormValueChange> values;

  public FormHistoryEntry() {
  }

  public String getFormId() {
    return formId;
  }

  public String getRecordId() {
    return recordId;
  }

  public int getTime() {
    return time;
  }

  public String getSubFieldId() {
    return subFieldId;
  }

  public String getSubFieldLabel() {
    return subFieldLabel;
  }

  public String getSubRecordKey() {
    return subRecordKey;
  }

  public String getChangeType() {
    return changeType;
  }

  public String getUserName() {
    return userName;
  }

  public String getUserEmail() {
    return userEmail;
  }

  public List<FormValueChange> getValues() {
    return values;
  }

  public static FormHistoryEntry fromJson(JsonValue jsonElement) {
      JsonValue jsonObject = jsonElement;
    FormHistoryEntry model = new FormHistoryEntry();
    model.formId = JsonParsing.toNullableString(jsonObject.get("formId"));
    model.recordId = JsonParsing.toNullableString(jsonObject.get("recordId"));
    model.time = jsonObject.get("time").asInt();
    model.subFieldId = JsonParsing.toNullableString(jsonObject.get("subFieldId"));
    model.subFieldLabel = JsonParsing.toNullableString(jsonObject.get("subFieldLabel"));
    model.subRecordKey = JsonParsing.toNullableString(jsonObject.get("subRecordKey"));
    model.changeType = JsonParsing.toNullableString(jsonObject.get("changeType"));
    model.userName = JsonParsing.toNullableString(jsonObject.get("userName"));
    model.userEmail = JsonParsing.toNullableString(jsonObject.get("userEmail"));
    model.values = FormValueChange.fromJsonArray(jsonObject.get("values"));
    return model;
  }

  public static List<FormHistoryEntry> fromJsonArray(JsonValue jsonArray) {
    List<FormHistoryEntry> list = new ArrayList<FormHistoryEntry>();
    for(JsonValue element : jsonArray.values()) {
      list.add(fromJson(element));
    }
    return list;
  }
  
  public static class Builder {

    private FormHistoryEntry entry = new FormHistoryEntry();

    /**
     * Sets the formId.
     *
     * @param formId id of the form
     */
    public Builder setFormId(String formId) {
      entry.formId = formId;
      return this;
    }

    /**
     * Sets the recordId.
     *
     * @param recordId id of the record
     */
    public Builder setRecordId(String recordId) {
      entry.recordId = recordId;
      return this;
    }

    /**
     * Sets the time.
     *
     * @param time the time, in seconds since 1970-01-01, that the change was made
     */
    public Builder setTime(int time) {
      entry.time = time;
      return this;
    }

    /**
     * Sets the subFieldId.
     *
     * @param subFieldId for sub records, the subForm field to which this sub record belongs
     */
    public Builder setSubFieldId(String subFieldId) {
      entry.subFieldId = subFieldId;
      return this;
    }

    /**
     * Sets the subFieldLabel.
     *
     * @param subFieldLabel for sub records, the label of the subForm field to which this sub record belongs
     */
    public Builder setSubFieldLabel(String subFieldLabel) {
      entry.subFieldLabel = subFieldLabel;
      return this;
    }

    /**
     * Sets the subRecordKey.
     *
     * @param subRecordKey for keyed sub forms, such as monthly, weekly, or daily subForms, this is a human readable label describing the key, for example '2016-06'
     */
    public Builder setSubRecordKey(String subRecordKey) {
      entry.subRecordKey = subRecordKey;
      return this;
    }

    /**
     * Sets the changeType.
     *
     */
    public Builder setChangeType(String changeType) {
      entry.changeType = changeType;
      return this;
    }

    /**
     * Sets the userName.
     *
     * @param userName the name of the user who made the change
     */
    public Builder setUserName(String userName) {
      entry.userName = userName;
      return this;
    }

    /**
     * Sets the userEmail.
     *
     * @param userEmail the email address of the user who made the change
     */
    public Builder setUserEmail(String userEmail) {
      entry.userEmail = userEmail;
      return this;
    }

    /**
     * Adds a value.
     *
     */
    public Builder addValue(FormValueChange value) {
      entry.values.add(value);
      return this;
    }

    public FormHistoryEntry build() {
      return entry;
    }
  }
}
