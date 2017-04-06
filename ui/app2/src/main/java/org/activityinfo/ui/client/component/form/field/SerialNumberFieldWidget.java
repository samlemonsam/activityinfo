package org.activityinfo.ui.client.component.form.field;

import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.SerialNumber;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.widget.TextBox;

/**
 * Widget for Serial number fields
 */
public class SerialNumberFieldWidget implements FormFieldWidget<SerialNumber> {

    private final TextBox box;

    public SerialNumberFieldWidget() {
        box = new TextBox();
        box.setReadOnly(true);
        box.setPlaceholder(I18N.CONSTANTS.pending());
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        // ALWAYS readonly
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public Promise<Void> setValue(SerialNumber value) {
        box.setValue(value.format());
        return Promise.resolved(null);
    }

    @Override
    public void setType(FieldType type) {
    }

    @Override
    public void clearValue() {
        box.setValue(null);
    }

    @Override
    public void fireValueChanged() {

    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Widget asWidget() {
        return box;
    }
}
