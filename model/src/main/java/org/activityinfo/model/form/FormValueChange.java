package org.activityinfo.model.form;

import jsinterop.annotations.JsType;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;

import java.util.ArrayList;
import java.util.List;

@JsType
public final class FormValueChange {
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
