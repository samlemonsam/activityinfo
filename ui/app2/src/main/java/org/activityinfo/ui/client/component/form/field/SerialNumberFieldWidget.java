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

import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.SerialNumber;
import org.activityinfo.model.type.SerialNumberType;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.widget.TextBox;

/**
 * Widget for Serial number fields
 */
public class SerialNumberFieldWidget implements FormFieldWidget<SerialNumber> {

    private final TextBox box;
    private SerialNumberType type;

    public SerialNumberFieldWidget(SerialNumberType type) {
        this.type = type;
        box = new TextBox();
        box.setReadOnly(true);
        box.setPlaceholder(I18N.CONSTANTS.pending());
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        // ALWAYS readonly
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public Promise<Void> setValue(SerialNumber value) {
        box.setValue(type.format(value));
        return Promise.resolved(null);
    }

    @Override
    public void setType(FieldType type) {
    }

    @Override
    public void clearValue() {
        box.setValue(null);
    }

    @Override
    public void fireValueChanged() {

    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Widget asWidget() {
        return box;
    }
}
