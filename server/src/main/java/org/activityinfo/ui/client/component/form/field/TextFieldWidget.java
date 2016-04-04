package org.activityinfo.ui.client.component.form.field;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.widget.TextBox;

public class TextFieldWidget implements FormFieldWidget<TextValue> {

    private final TextBox box;
    private final ValueUpdater<TextValue> valueUpdater;

    public TextFieldWidget(final ValueUpdater<TextValue> valueUpdater) {
        this.valueUpdater = valueUpdater;
        this.box = new TextBox();
        this.box.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                valueUpdater.update(TextValue.valueOf(event.getValue()));
            }
        });
        this.box.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                fireValueChanged();
            }
        });
    }

    @Override
    public void fireValueChanged() {
        valueUpdater.update(getValue());
    }

    private TextValue getValue() {
        return TextValue.valueOf(TextFieldWidget.this.box.getValue());
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        box.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() {
        return box.isReadOnly();
    }

    @Override
    public Promise<Void> setValue(TextValue value) {
        box.setValue(value.toString());
        return Promise.done();
    }

    @Override
    public void clearValue() {
        box.setValue(null);
    }

    @Override
    public void setType(FieldType type) {
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
