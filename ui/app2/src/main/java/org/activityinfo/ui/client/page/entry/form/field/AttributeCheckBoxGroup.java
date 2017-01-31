package org.activityinfo.ui.client.page.entry.form.field;

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

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.google.common.collect.Lists;
import org.activityinfo.legacy.shared.model.AttributeDTO;
import org.activityinfo.legacy.shared.model.AttributeGroupDTO;
import org.activityinfo.legacy.shared.model.SiteDTO;

import java.util.List;

public class AttributeCheckBoxGroup extends CheckBoxGroup implements AttributeField {

    private List<CheckBox> checkBoxes = Lists.newArrayList();
    private CheckBox checkBoxWithDefaultValue = null;

    public AttributeCheckBoxGroup(AttributeGroupDTO group) {
        assert group != null;
        String name = group.getName();
        if (group.isMandatory()) {
            name += "*";
            this.setValidator(new Validator() {
                @Override
                public String validate(Field<?> field, String value) {
                    for (CheckBox box : checkBoxes) {
                        // return null (== no validation error) if at least one checkbox has been ticked
                        if (Boolean.TRUE.equals(box.getValue())) {
                            return null;
                        }
                    }
                    return "invalid";
                }
            });
        }
        this.setFieldLabel(Format.htmlEncode(name));
        this.setOrientation(Orientation.VERTICAL);

        for (AttributeDTO attrib : group.getAttributes()) {
            CheckBox box = new CheckBox();
            box.setBoxLabel(attrib.getName());
            box.setName(attrib.getPropertyName());

            if (group.getDefaultValue() != null && attrib.getId() == group.getDefaultValue()) {
                checkBoxWithDefaultValue = box;
            }

            this.add(box);
            checkBoxes.add(box);
        }
    }

    @Override
    public void updateForm(SiteDTO site, boolean isNew) {
        for (CheckBox checkBox : checkBoxes) {
            checkBox.setValue(site.<Boolean>get(checkBox.getName()));
        }
        if (isNew && checkBoxWithDefaultValue != null) {
            checkBoxWithDefaultValue.setValue(true);
        }
    }

    @Override
    public void updateModel(SiteDTO site) {
        for (CheckBox checkBox : checkBoxes) {
            site.set(checkBox.getName(), checkBox.getValue());
        }
    }
}
