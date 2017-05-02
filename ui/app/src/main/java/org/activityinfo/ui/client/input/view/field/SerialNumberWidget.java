package org.activityinfo.ui.client.input.view.field;

import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.form.TextField;
import org.activityinfo.i18n.shared.I18N;

/**
 * FieldWidget for {@link org.activityinfo.model.type.SerialNumberType} fields.
 *
 * <p>Only displays values and does not allow input.</p>
 */
public class SerialNumberWidget implements FieldWidget {

    private TextField field;

    public SerialNumberWidget() {

        this.field = new TextField();
        this.field.setReadOnly(true);
        this.field.setEmptyText(I18N.CONSTANTS.pending());
    }

    @Override
    public void setRelevant(boolean relevant) {

    }

    @Override
    public Widget asWidget() {
        return field;
    }
}
