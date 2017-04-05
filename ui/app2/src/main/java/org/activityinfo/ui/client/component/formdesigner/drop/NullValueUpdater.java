package org.activityinfo.ui.client.component.formdesigner.drop;

import org.activityinfo.ui.client.component.form.field.FieldUpdater;

public enum NullValueUpdater implements FieldUpdater {
    INSTANCE {

    };

    @Override
    public void update(Object value) {
        // No action
    }

    @Override
    public void onInvalid(String errorMessage) {
        // No action
    }
}
