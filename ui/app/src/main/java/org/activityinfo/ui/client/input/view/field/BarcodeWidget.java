package org.activityinfo.ui.client.input.view.field;

import com.google.common.base.Strings;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.TextInputCell;
import com.sencha.gxt.widget.core.client.form.TextField;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.barcode.BarcodeValue;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.ui.client.input.model.FieldInput;

/**
 * FieldWidget for {@link TextType} fields.
 */
public class BarcodeWidget implements FieldWidget {

    private final TextField field;

    public BarcodeWidget(FieldUpdater updater) {
        field = new TextField(new TextInputCell());
        field.addKeyUpHandler(event -> updater.update(input()));
    }

    private FieldInput input() {
        String value = field.getText();
        if(Strings.isNullOrEmpty(value)) {
            return FieldInput.EMPTY;
        } else {
            return new FieldInput(BarcodeValue.valueOf(value));
        }
    }

    @Override
    public Widget asWidget() {
        return field;
    }

    @Override
    public void init(FieldValue value) {
        field.setText(((TextValue) value).asString());
    }

    @Override
    public void clear() {
        field.clear();
    }

    @Override
    public void setRelevant(boolean relevant) {
        field.setEnabled(relevant);
    }
}
