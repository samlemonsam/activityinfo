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
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.ui.client.input.model.FieldInput;

import java.util.HashMap;
import java.util.Map;

/**
 * Displays a single-select enum field as a ComboBox
 */
public class DropDownEnumWidget implements FieldWidget {


    public static final String NON_BREAKING_SPACE = "\u00A0";
    private static final EnumItem MISSING = new EnumItem(ResourceId.valueOf("$"), NON_BREAKING_SPACE);

    private ComboBox<EnumItem> comboBox;
    private final ListStore<EnumItem> store;
    private final Map<ResourceId, String> labels = new HashMap<>();

    public DropDownEnumWidget(FormField field, EnumType type, FieldUpdater updater) {

        for (EnumItem enumItem : type.getValues()) {
            labels.put(enumItem.getId(), enumItem.getLabel());
        }

        store = new ListStore<>(item -> item.getId().asString());
        if(!field.isRequired()) {
            store.add(MISSING);
        }
        store.addAll(type.getValues());

        comboBox = new ComboBox<>(store, item -> item.getLabel());
        comboBox.setForceSelection(true);
        comboBox.setTriggerAction(ComboBoxCell.TriggerAction.ALL);
        comboBox.addSelectionHandler(event -> {
           if(event.getSelectedItem() == null || event.getSelectedItem() == MISSING) {
               updater.update(FieldInput.EMPTY);
           } else {
               updater.update(new FieldInput(new EnumValue(event.getSelectedItem().getId())));
           }
        });
        comboBox.addBlurHandler(event -> updater.touch());
    }

    @Override
    public void init(FieldValue value) {

        EnumValue enumValue = (EnumValue) value;
        String label = labels.get(enumValue.getValueId());

        comboBox.setText(label);
    }

    @Override
    public void clear() {
        comboBox.clear();
    }

    @Override
    public void setRelevant(boolean relevant) {
        comboBox.setEnabled(relevant);
    }

    @Override
    public void focus() {
        comboBox.focus();
    }

    @Override
    public Widget asWidget() {
        return comboBox;
    }
}
