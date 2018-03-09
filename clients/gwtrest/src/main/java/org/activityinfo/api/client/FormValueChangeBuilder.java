/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.api.client;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;

public class FormValueChangeBuilder {
  private JsonValue jsonObject = Json.createObject();

  public FormValueChangeBuilder() {
  }

  public String toJsonString() {
    return jsonObject.toJson();
  }

  public JsonValue toJsonObject() {
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
