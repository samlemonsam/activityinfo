/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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


    private InputMask inputMask;
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
        this.inputMask = new InputMask(((TextType) type).getInputMask());
        this.box.setPlaceholder(inputMask.placeHolderText());
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
