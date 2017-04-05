package org.activityinfo.ui.client.component.form.field;

import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.primitive.InputMask;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.widget.TextBox;

public class TextFieldWidget implements FormFieldWidget<TextValue> {


    private final InputMask inputMask;
    private final TextBox box;
    private final FieldUpdater valueUpdater;


    public TextFieldWidget(TextType type, final FieldUpdater valueUpdater) {
        this.valueUpdater = valueUpdater;
        this.inputMask = new InputMask(type.getInputMask());
        this.box = new TextBox();
        this.box.setPlaceholder(inputMask.placeHolderText());
        this.box.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                valueUpdater.update(TextValue.valueOf(event.getValue()));
            }
        });
        this.box.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                onInput();
            }
        });
        this.box.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onInput();
            }
        });
    }


    @Override
    public void fireValueChanged() {
        valueUpdater.update(TextValue.valueOf(box.getValue()));
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
        box.setValue(value.asString());
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


    private void onInput() {
        String text = box.getText();

        if(Strings.isNullOrEmpty(text)) {
            valueUpdater.update(null);

        } else if(inputMask.isValid(text)) {
            valueUpdater.update(TextValue.valueOf(text));

        } else {
            valueUpdater.onInvalid(I18N.MESSAGES.invalidTextInput(inputMask.placeHolderText()));
        }
    }

}
