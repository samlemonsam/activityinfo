package org.activityinfo.ui.client.input.view.field;

import com.google.common.base.Strings;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.form.TextField;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.ui.client.input.model.FieldInput;

public class TextWidget implements FieldWidget {

    private final TextField field;

    public TextWidget(FieldUpdater updater) {
        field = new TextField();
        field.addKeyUpHandler(event -> updater.update(input()));
    }

    private FieldInput input() {
        String value = field.getValue();
        if(Strings.isNullOrEmpty(value)) {
            return FieldInput.EMPTY;
        } else {
            return new FieldInput(TextValue.valueOf(value));
        }
    }

    @Override
    public Widget asWidget() {
        return field;
    }

    @Override
    public void setRelevant(boolean relevant) {
        field.setEnabled(relevant);
    }
}
