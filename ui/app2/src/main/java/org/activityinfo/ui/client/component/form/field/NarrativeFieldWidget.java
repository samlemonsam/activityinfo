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

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.NarrativeValue;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.widget.TextArea;

public class NarrativeFieldWidget implements FormFieldWidget<NarrativeValue> {

    private final TextArea textArea;
    private final ValueUpdater<NarrativeValue> updater;

    public NarrativeFieldWidget(final ValueUpdater<NarrativeValue> updater) {
        this.updater = updater;
        this.textArea = new TextArea();
        this.textArea.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                fireValueChanged();
            }
        });
        this.textArea.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                fireValueChanged();
            }
        });
    }

    @Override
    public void fireValueChanged() {
        updater.update(NarrativeValue.valueOf(this.textArea.getText()));
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        textArea.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() {
        return textArea.isReadOnly();
    }

    @Override
    public Promise<Void> setValue(NarrativeValue value) {
        textArea.setValue(value.getText());
        return Promise.done();
    }

    @Override
    public void clearValue() {
        textArea.setValue(null);
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
        return textArea;
    }
}
