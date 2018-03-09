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
package org.activityinfo.ui.client.input.view.field;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.TextInputCell;
import com.sencha.gxt.widget.core.client.form.TextField;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.ui.client.input.model.FieldInput;

/**
 * FieldWidget for {@link org.activityinfo.model.type.primitive.TextType} fields.
 */
public class TextWidget implements FieldWidget {

    private final TextField field;

    public TextWidget(TextType textType, FieldUpdater updater) {
        this(textType, GWT.create(TextInputCell.TextFieldAppearance.class), updater);
    }

    public TextWidget(TextType textType, TextInputCell.TextFieldAppearance appearance, FieldUpdater updater) {
        field = new TextField(new TextInputCell(appearance));
        field.addKeyUpHandler(event -> updater.update(input()));

        if(textType.hasInputMask()) {
            field.setEmptyText(textType.getInputMask());
        }
    }

    private FieldInput input() {
        String value = field.getText();
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
