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
import org.activityinfo.model.type.barcode.BarcodeValue;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.widget.TextBox;

public class BarcodeFieldWidget implements FormFieldWidget<BarcodeValue> {

    private final TextBox box;

    private final ValueUpdater<BarcodeValue> valueUpdater;

    public BarcodeFieldWidget(final ValueUpdater<BarcodeValue> valueUpdater) {
        this.valueUpdater = valueUpdater;
        this.box = new TextBox();
        this.box.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                fireValueChanged();
            }

        });
        this.box.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                valueUpdater.update(getValue());
            }
        });
    }

    @Override
    public void fireValueChanged() {
        valueUpdater.update(getValue());
    }

    @Override
    public boolean isValid() {
        return true;
    }

    private BarcodeValue getValue() {
        return BarcodeValue.valueOf(BarcodeFieldWidget.this.box.getValue());
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
    public Promise<Void> setValue(BarcodeValue value) {
        box.setValue(value.getCode());
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
    public Widget asWidget() {
        return box;
    }
}
