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

import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.form.DateField;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.ui.client.input.model.FieldInput;

import java.util.Collections;
import java.util.List;

/**
 * FieldWidget for {@link org.activityinfo.model.type.time.LocalDateType} fields.
 */
public class LocalDateWidget implements PeriodFieldWidget {

    private DateField field;

    public LocalDateWidget(FieldUpdater fieldUpdater) {
        this.field = new DateField();
        // Ensure that no date can be selected which occurs before the minimum date set on LocalDate
        this.field.setMinValue(LocalDate.MIN_DATE.atMidnightInMyTimezone());
        this.field.setPropertyEditor(new LocalDateTimePropertyEditor());
        this.field.addValueChangeHandler(event -> fieldUpdater.update(input()));
    }

    private FieldInput input() {
        if(field.isValid()) {
            if(field.getValue() == null) {
                return FieldInput.EMPTY;
            } else {
                return new FieldInput(new LocalDate(field.getValue()));
            }
        } else {
            return FieldInput.INVALID_INPUT;
        }
    }

    @Override
    public void init(FieldValue value) {
        field.setValue(((LocalDate) value).atMidnightInMyTimezone());
    }

    @Override
    public void clear() {
        field.clear();
    }

    @Override
    public void setRelevant(boolean relevant) {
        field.setEnabled(relevant);
    }

    @Override
    public Widget asWidget() {
        return field;
    }

    @Override
    public List<Component> asToolBarItems() {
        return Collections.singletonList(field);
    }
}
