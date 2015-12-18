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

import java.util.Set;

/**
 * @author yuriyz on 09/28/2015.
 */
public class EnumComboboxWidget implements FormFieldWidget<EnumValue> {

    private final ListBox dropBox;
    private final ValueUpdater<EnumValue> valueUpdater;

    public EnumComboboxWidget(EnumType enumType, final ValueUpdater<EnumValue> valueUpdater) {
        this.valueUpdater = valueUpdater;
        dropBox = new ListBox(enumType.getCardinality() == Cardinality.MULTIPLE);
        dropBox.addStyleName("form-control");

        for (EnumItem enumItem : enumType.getValues()) {
            dropBox.addItem(enumItem.getLabel(), enumItem.getId().asString());
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
    public void setReadOnly(boolean readOnly) {
        dropBox.setEnabled(!readOnly);
    }

    private EnumValue updatedValue() {
        Set<ResourceId> value = Sets.newHashSet();
        for (int i = 0; i < dropBox.getItemCount(); i++) {
            if (dropBox.isItemSelected(i)) {
                value.add(ResourceId.valueOf(dropBox.getValue(i)));
            }
        }
        return new EnumValue(value);
    }

    @Override
    public Promise<Void> setValue(EnumValue value) {
        for (int i = 0; i != dropBox.getItemCount(); ++i) {
            ResourceId id = ResourceId.valueOf(dropBox.getValue(i));
            dropBox.setItemSelected(i, value.getResourceIds().contains(id));
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
