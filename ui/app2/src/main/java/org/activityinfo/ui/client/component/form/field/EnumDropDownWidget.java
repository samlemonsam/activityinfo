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

import com.google.common.collect.Sets;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.promise.Promise;

import java.util.List;
import java.util.Set;

/**
 * @author yuriyz on 09/28/2015.
 */
public class EnumDropDownWidget implements FormFieldWidget<EnumValue> {

    private final ListBox dropBox;
    private final EnumType enumType;
    private final ValueUpdater<EnumValue> valueUpdater;

    public EnumDropDownWidget(EnumType enumType, final ValueUpdater<EnumValue> valueUpdater) {
        this.enumType = enumType;
        this.valueUpdater = valueUpdater;
        dropBox = new ListBox(enumType.getCardinality() == Cardinality.MULTIPLE);
        dropBox.addStyleName("form-control");

        // Empty value
        dropBox.addItem("");

        for (EnumItem enumItem : enumType.getValues()) {
            dropBox.addItem(enumItem.getLabel());
        }
        dropBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                fireValueChanged();
            }
        });
    }

    @Override
    public void fireValueChanged() {
        valueUpdater.update(updatedValue());
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        dropBox.setEnabled(!readOnly);
    }

    @Override
    public boolean isReadOnly() {
        return !dropBox.isEnabled();
    }

    private EnumValue updatedValue() {
        if(dropBox.getSelectedIndex() <= 0) {
            return null;
        }
        EnumItem enumItem = enumType.getValues().get(dropBox.getSelectedIndex() - 1);
        return new EnumValue(enumItem.getId());
    }

    @Override
    public Promise<Void> setValue(EnumValue value) {

        dropBox.setSelectedIndex(-1);

        if(value.getResourceIds().size() == 1) {
            List<EnumItem> values = enumType.getValues();
            for (int i = 0; i < values.size(); i++) {
                if (values.get(i).getId().equals(value.getValueId())) {
                    dropBox.setSelectedIndex(i + 1);
                    break;
                }
            }
        }
        return Promise.done();
    }

    @Override
    public void clearValue() {
        setValue(EnumValue.EMPTY);
    }

    @Override
    public void setType(FieldType type) {
    }

    @Override
    public Widget asWidget() {
        return dropBox;
    }
}
