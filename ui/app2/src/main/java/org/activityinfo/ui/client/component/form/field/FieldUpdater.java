package org.activityinfo.ui.client.component.form.field;

import com.google.gwt.cell.client.ValueUpdater;
import org.activityinfo.model.type.FieldValue;

/**
 * Handles changes to the field values from the User
 */
public interface FieldUpdater<T extends FieldValue> extends ValueUpdater<T> {

    void onInvalid(String errorMessage);

}
