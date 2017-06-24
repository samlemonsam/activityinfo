package org.activityinfo.api.client;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonObject;

public class FormValueChangeBuilder {
  private JsonObject jsonObject = Json.createObject();

  public FormValueChangeBuilder() {
  }

  public String toJsonString() {
    return jsonObject.toJson();
  }

  public JsonObject toJsonObject() {
    return jsonObject;
  }

  /**
   * Sets the fieldId.
   *
   * @param fieldId the id of the field changed
   */
  public FormValueChangeBuilder setFieldId(String fieldId) {
    this.jsonObject.put("fieldId", fieldId);
    return this;
  }

  /**
   * Sets the fieldLabel.
   *
   * @param fieldLabel the current label of the field changed
   */
  public FormValueChangeBuilder setFieldLabel(String fieldLabel) {
    this.jsonObject.put("fieldLabel", fieldLabel);
    return this;
  }

  /**
   * Sets the oldValueLabel.
   *
   * @param oldValueLabel human-readable string of the old value of the field
   */
  public FormValueChangeBuilder setOldValueLabel(String oldValueLabel) {
    this.jsonObject.put("oldValueLabel", oldValueLabel);
    return this;
  }

  /**
   * Sets the newValueLabel.
   *
   * @param newValueLabel human-readable string of the new value of the field
   */
  public FormValueChangeBuilder setNewValueLabel(String newValueLabel) {
    this.jsonObject.put("newValueLabel", newValueLabel);
    return this;
  }

  /**
   * Sets the subFormKind.
   *
   * @param subFormKind subform kind
   */
  public FormValueChangeBuilder setSubFormKind(String subFormKind) {
    this.jsonObject.put("subFormKind", subFormKind);
    return this;
  }

  /**
   * Sets the subFormKey.
   *
   * @param subFormKey subform key
   */
  public FormValueChangeBuilder setSubFormKey(String subFormKey) {
    this.jsonObject.put("subFormKey", subFormKey);
    return this;
  }
}
