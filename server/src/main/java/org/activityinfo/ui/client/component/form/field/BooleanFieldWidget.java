package org.activityinfo.ui.client.component.form.field;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.promise.Promise;

/**
 * @author yuriyz on 7/21/14.
 */
public class BooleanFieldWidget implements FormFieldWidget<BooleanFieldValue> {

    private final CheckBox checkBox;

    public BooleanFieldWidget(final ValueUpdater valueUpdater) {
        this.checkBox = new CheckBox();
        this.checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                valueUpdater.update(event.getValue());
            }
        });
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        checkBox.setEnabled(!readOnly);
    }

    @Override
    public Promise<Void> setValue(BooleanFieldValue value) {
        checkBox.setValue(value.asBoolean());
        return Promise.done();
    }

    @Override
    public void clearValue() {
        checkBox.setValue(false);
    }

    @Override
    public void setType(FieldType type) {

    }

    @Override
    public Widget asWidget() {
        return checkBox;
    }
}
