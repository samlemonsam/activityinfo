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
package org.activityinfo.ui.client.component.formdesigner.properties;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.ui.client.widget.TextBox;


public class TextTypeEditor extends TypeEditor<TextType> {

    interface TextTypeEditorUiBinder extends UiBinder<FlowPanel, TextTypeEditor> {
    }

    private static TextTypeEditorUiBinder ourUiBinder = GWT.create(TextTypeEditorUiBinder.class);

    private final FlowPanel panel;

    @UiField
    TextBox inputMask;

    public TextTypeEditor() {
        this.panel = ourUiBinder.createAndBindUi(this);
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

    @Override
    protected boolean accept(FieldType type) {
        return type instanceof TextType;
    }

    @Override
    protected void show(TextType type) {
        inputMask.setText(type.getInputMask());
    }

    @UiHandler("inputMask")
    public void onInputMaskChanged(KeyUpEvent event) {
        updateType(TextType.SIMPLE.withInputMask(inputMask.getText()));
    }
}
