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
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.widget.DoubleBox;

import java.text.ParseException;

public class QuantityFieldWidget implements FormFieldWidget<Quantity> {

    private final FlowPanel panel;
    private final DoubleBox box;
    private final InlineLabel unitsLabel;
    private final FieldUpdater valueUpdater;
    private final QuantityType type;

    public QuantityFieldWidget(final QuantityType type, final FieldUpdater valueUpdater) {
        this.type = type;
        this.valueUpdater = valueUpdater;

        box = new DoubleBox();
        box.addValueChangeHandler(new ValueChangeHandler<Double>() {
            @Override
            public void onValueChange(ValueChangeEvent<Double> event) {
                onValueChanged();
            }
        });
        box.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onValueChanged();
            }
        });

        unitsLabel = new InlineLabel(type.getUnits());
        unitsLabel.setStyleName("input-group-addon");

        panel = new FlowPanel();
        panel.setStyleName("input-group");
        panel.add(box);
        panel.add(unitsLabel);
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
    public Promise<Void> setValue(Quantity value) {
        box.setValue(value != null ? value.getValue() : null);
        return Promise.done();
    }

    @Override
    public void clearValue() {
        box.setValue(null);
    }

    @Override
    public void fireValueChanged() {
        onValueChanged();
    }

    @Override
    public void setType(FieldType type) {
        unitsLabel.setText(((QuantityType) type).getUnits());
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

    public boolean isValid() {
        return Strings.isNullOrEmpty(box.getText()) ||
                (!Strings.isNullOrEmpty(box.getText()) && box.getValue() != null);
    }

    public void onValueChanged() {

        try {
            Double value = box.getValueOrThrow();
            if(value == null) {
                valueUpdater.update(null);
            } else {
                valueUpdater.update(new Quantity(value));
            }
        } catch (ParseException e) {
            valueUpdater.onInvalid(invalidErrorMessage());
        }
    }


    private String invalidErrorMessage() {
        NumberFormat decimalFormat = NumberFormat.getDecimalFormat();
        return I18N.MESSAGES.quantityFieldInvalidValue(15, decimalFormat.format(2000), decimalFormat.format(1.5));
    }
}
