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

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.TextAreaInputCell;
import com.sencha.gxt.widget.core.client.form.TextArea;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.NarrativeValue;
import org.activityinfo.ui.client.input.model.FieldInput;

/**
 * FieldWidget for {@link org.activityinfo.model.type.NarrativeType} fields.
 */
public class NarrativeWidget implements FieldWidget {

    private class NarrativeTextArea extends TextArea {

        private final FieldUpdater updater;

        NarrativeTextArea(TextAreaInputCell cell, FieldUpdater updater) {
            super(cell);
            this.updater = updater;
            sinkEvents(Event.ONPASTE);
        }

        @Override
        public void onBrowserEvent(Event event) {
            super.onBrowserEvent(event);
            if (event.getTypeInt() == Event.ONPASTE) {
                Scheduler.get().scheduleDeferred(() -> updater.update(input()));
            }
        }
    }

    private TextArea textArea;

    public NarrativeWidget(FieldUpdater updater) {
        this(GWT.create(TextAreaInputCell.TextAreaAppearance.class), updater);
    }

    public NarrativeWidget(TextAreaInputCell.TextAreaAppearance appearance, FieldUpdater updater) {
        textArea = new NarrativeTextArea(new TextAreaInputCell(appearance), updater);

        textArea.addKeyUpHandler(event -> updater.update(input()));
        textArea.addValueChangeHandler(event -> updater.update(input()));

        textArea.setWidth(-1);
    }

    private FieldInput input() {
        return new FieldInput(NarrativeValue.valueOf(textArea.getText()));
    }

    @Override
    public void init(FieldValue value) {
        textArea.setText(((NarrativeValue) value).asString());
    }

    @Override
    public void clear() {
        textArea.clear();
    }

    @Override
    public void setRelevant(boolean relevant) {
        textArea.setEnabled(relevant);
    }

    @Override
    public Widget asWidget() {
        return textArea;
    }
}
