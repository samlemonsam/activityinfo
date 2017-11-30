package org.activityinfo.model.form;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;

public final class FormValueChange implements JsonSerializable {
    private String fieldId;
    private String fieldLabel;
    private String oldValueLabel;
    private String newValueLabel;
    private String subFormKind;
    private String subFormKey;

    public FormValueChange() {
    }

    public String getFieldId() {
        return fieldId;
    }

    public String getFieldLabel() {
        return fieldLabel;
    }

    public String getOldValueLabel() {
        return oldValueLabel;
    }

    public String getNewValueLabel() {
        return newValueLabel;
    }

    public String getSubFormKind() {
        return subFormKind;
    }

    public String getSubFormKey() {
        return subFormKey;
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("fieldId", fieldId);
        object.put("fieldLabel", fieldLabel);
        object.put("oldValueLabel", oldValueLabel);
        object.put("newValueLabel", newValueLabel);
        object.put("subFormKind", subFormKind);
        object.put("subFormKey", subFormKey);

        return object;
    }

    public static FormValueChange fromJson(JsonValue object) {
        FormValueChange change = new FormValueChange();
        change.fieldId = object.getString("fieldId");
        change.fieldLabel = object.getString("fieldLabel");
        change.oldValueLabel = object.getString("oldValueLabel");
        change.newValueLabel = object.getString("newValueLabel");
        change.subFormKind = object.getString("subFormKind");
        change.subFormKey = object.getString("subFormKey");
        return change;
    }

    public static class Builder {

        private FormValueChange change = new FormValueChange();

        public Builder setFieldId(String fieldId) {
            change.fieldId = fieldId;
            return this;
        }

        /**
         * Sets the fieldLabel.
         *
         * @param fieldLabel the current label of the field changed
         */
        public Builder setFieldLabel(String fieldLabel) {
            change.fieldLabel = fieldLabel;
            return this;
        }

        /**
         * Sets the oldValueLabel.
         *
         * @param oldValueLabel human-readable string of the old value of the field
         */
        public Builder setOldValueLabel(String oldValueLabel) {
            change.oldValueLabel = oldValueLabel;
            return this;
        }

        /**
         * Sets the newValueLabel.
         *
         * @param newValueLabel human-readable string of the new value of the field
         */
        public Builder setNewValueLabel(String newValueLabel) {
            change.newValueLabel = newValueLabel;
            return this;
        }

        /**
         * Sets the subFormKind.
         *
         * @param subFormKind subform kind
         */
        public Builder setSubFormKind(String subFormKind) {
            change.subFormKind = subFormKind;
            return this;
        }

        /**
         * Sets the subFormKey.
         *
         * @param subFormKey subform key
         */
        public Builder setSubFormKey(String subFormKey) {
            change.subFormKey = subFormKey;
            return this;
        }

        public FormValueChange build() {
            return change;
        }
    }
}
