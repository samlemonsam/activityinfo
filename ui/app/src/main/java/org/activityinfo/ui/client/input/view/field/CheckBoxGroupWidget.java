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

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.ui.client.input.model.FieldInput;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * FieldWidget for {@link EnumType} fields with multiple cardinality that presents the options
 * as check boxes.
 */
public class CheckBoxGroupWidget implements FieldWidget {

    private FlowPanel flowPanel;
    private Map<ResourceId, CheckBox> checkBoxes = new HashMap<>();

    private boolean relevant = true;

    public CheckBoxGroupWidget(EnumType enumType, FieldUpdater fieldUpdater) {

        this.flowPanel = new FlowPanel();
        for (EnumItem enumItem : enumType.getValues()) {
            CheckBox checkBox = new CheckBox();
            checkBox.setBoxLabel(enumItem.getLabel());
            checkBox.addValueChangeHandler(event -> fieldUpdater.update(input()));
            checkBoxes.put(enumItem.getId(), checkBox);
            flowPanel.add(checkBox);
        }
    }

    private FieldInput input() {
        Set<ResourceId> items = new HashSet<>();
        for (Map.Entry<ResourceId, CheckBox> entry : checkBoxes.entrySet()) {
            if(entry.getValue().getValue()) {
                items.add(entry.getKey());
            }
        }
        if(items.isEmpty()) {
            return FieldInput.EMPTY;
        } else {
            return new FieldInput(new EnumValue(items));
        }
    }

    @Override
    public void init(FieldValue value) {
        EnumValue enumValue = (EnumValue) value;
        for (ResourceId itemId : enumValue.getResourceIds()) {
            CheckBox checkBox = checkBoxes.get(itemId);
            checkBox.setValue(true);
        }
    }

    @Override
    public void clear() {
        for (CheckBox checkBox : checkBoxes.values()) {
            checkBox.setValue(false);
        }
    }

    @Override
    public void setRelevant(boolean relevant) {
        if (this.relevant != relevant) {
            this.relevant = relevant;
            for (CheckBox checkBox : checkBoxes.values()) {
                checkBox.setEnabled(relevant);
            }
        }
    }

    @Override
    public void focus() {
        if(!checkBoxes.isEmpty()) {
            checkBoxes.get(0).focus();
        }
    }

    @Override
    public Widget asWidget() {
        return flowPanel;
    }
}
