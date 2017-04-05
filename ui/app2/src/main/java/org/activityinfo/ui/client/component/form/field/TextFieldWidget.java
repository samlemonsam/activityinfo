package org.activityinfo.ui.client.component.form.field;

import com.google.common.base.Strings;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.primitive.InputMask;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.form.event.FieldMessageEvent;
import org.activityinfo.ui.client.widget.TextBox;

public class TextFieldWidget implements FormFieldWidget<TextValue> {


    private enum State {
        EMPTY,
        INVALID,
        VALID
    }

    private final InputMask inputMask;
    private final TextBox box;
    private final ValueUpdater<TextValue> valueUpdater;
    private EventBus eventBus;
    private ResourceId fieldId;

    private String currentValue;
    private State currentState = State.EMPTY;


    public TextFieldWidget(TextType type, final ValueUpdater<TextValue> valueUpdater, EventBus eventBus, ResourceId fieldId) {
        this.valueUpdater = valueUpdater;
        this.eventBus = eventBus;
        this.fieldId = fieldId;
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
        valueUpdater.update(TextValue.valueOf(currentValue));
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
        if (inputMask.isValid(value.asString())) {
            currentState = State.VALID;
        } else {
            currentState = State.INVALID;
        }
        currentValue = value.asString();
        box.setValue(currentValue);
        return Promise.done();
    }

    @Override
    public void clearValue() {
        this.currentState = State.EMPTY;
        this.currentValue = null;
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
        currentValue = text;

        if(Strings.isNullOrEmpty(text)) {
            valueUpdater.update(null);
            if(currentState != State.EMPTY) {
                eventBus.fireEvent(new FieldMessageEvent(fieldId, "").setClearMessage(true));
                currentState = State.EMPTY;
            }

        } else if(inputMask.isValid(text)) {
            valueUpdater.update(TextValue.valueOf(text));
            if(currentState != State.VALID) {
                eventBus.fireEvent(new FieldMessageEvent(fieldId, "").setClearMessage(true));
                currentState = State.VALID;
            }

        } else {
            if(currentState != State.INVALID) {
                eventBus.fireEvent(new FieldMessageEvent(fieldId, "Bad Value"));
                currentState = State.INVALID;
            }
        }

    }

}
