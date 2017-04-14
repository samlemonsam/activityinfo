package org.activityinfo.ui.client.input.view.field;

import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.form.TextField;
import org.activityinfo.i18n.shared.I18N;

public class SerialNumberEditor implements FieldWidget {

    private TextField field;

    public SerialNumberEditor() {
        this.field = new TextField();
        this.field.setReadOnly(true);
        this.field.getElement().setAttribute("placeholder", I18N.CONSTANTS.pending());
    }

    @Override
    public void setRelevant(boolean relevant) {

    }

    @Override
    public Widget asWidget() {
        return field;
    }
}
