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
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.NumberInputCell;
import com.sencha.gxt.widget.core.client.form.DoubleField;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.ui.client.input.model.FieldInput;

import java.text.ParseException;

/**
 * FieldWidget for {@link QuantityType} fields.
 */
public class QuantityWidget implements FieldWidget {

    private DoubleField field;

    public QuantityWidget(QuantityType quantityType, FieldUpdater updater) {
        this.field = new DoubleField(new NumberInputCell<>(
                new NumberPropertyEditor.DoublePropertyEditor(),
                new QuantityFieldAppearance(quantityType.getUnits())));
        this.field.addKeyUpHandler(event -> Scheduler.get().scheduleDeferred(() -> {
            updater.update(input());
        }));

    }

    private FieldInput input() {
        String text = field.getText();
        if(Strings.isNullOrEmpty(text)) {
            return FieldInput.EMPTY;
        } else {
            double doubleValue;
            try {
                doubleValue = field.getPropertyEditor().parse(text);
                return new FieldInput(new Quantity(doubleValue));
            } catch (ParseException e) {
                return FieldInput.INVALID_INPUT;
            }
        }
    }

    @Override
    public void init(FieldValue value) {
        this.field.setValue(((Quantity) value).getValue());
    }

    @Override
    public void clear() {
        field.clear();
    }

    @Override
    public void setRelevant(boolean relevant) {
        this.field.setEnabled(relevant);
    }

    @Override
    public Widget asWidget() {
        return field;
    }
}
