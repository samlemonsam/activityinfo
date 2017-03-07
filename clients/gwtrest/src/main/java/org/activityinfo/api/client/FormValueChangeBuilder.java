package org.activityinfo.api.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class FormValueChangeBuilder {
  private JsonObject jsonObject = new JsonObject();

  public FormValueChangeBuilder() {
  }

  public String toJsonString() {
    return jsonObject.toString();
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
    this.jsonObject.add("fieldId", new JsonPrimitive(fieldId));
    return this;
  }

  /**
   * Sets the fieldLabel.
   *
   * @param fieldLabel the current label of the field changed
   */
  public FormValueChangeBuilder setFieldLabel(String fieldLabel) {
    this.jsonObject.add("fieldLabel", new JsonPrimitive(fieldLabel));
    return this;
  }

  /**
   * Sets the oldValueLabel.
   *
   * @param oldValueLabel human-readable string of the old value of the field
   */
  public FormValueChangeBuilder setOldValueLabel(String oldValueLabel) {
    this.jsonObject.add("oldValueLabel", new JsonPrimitive(oldValueLabel));
    return this;
  }

  /**
   * Sets the newValueLabel.
   *
   * @param newValueLabel human-readable string of the new value of the field
   */
  public FormValueChangeBuilder setNewValueLabel(String newValueLabel) {
    this.jsonObject.add("newValueLabel", new JsonPrimitive(newValueLabel));
    return this;
  }

  /**
   * Sets the subFormKind.
   *
   * @param subFormKind subform kind
   */
  public FormValueChangeBuilder setSubFormKind(String subFormKind) {
    this.jsonObject.add("subFormKind", new JsonPrimitive(subFormKind));
    return this;
  }

  /**
   * Sets the subFormKey.
   *
   * @param subFormKey subform key
   */
  public FormValueChangeBuilder setSubFormKey(String subFormKey) {
    this.jsonObject.add("subFormKey", new JsonPrimitive(subFormKey));
    return this;
  }
}
