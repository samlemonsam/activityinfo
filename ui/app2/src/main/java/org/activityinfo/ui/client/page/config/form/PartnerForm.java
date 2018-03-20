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
package org.activityinfo.ui.client.page.config.form;

import com.extjs.gxt.ui.client.binding.FieldBinding;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.core.client.GWT;
import org.activityinfo.i18n.shared.UiConstants;
import org.activityinfo.legacy.shared.model.PartnerDTO;
import org.activityinfo.ui.client.page.config.design.UniqueNameValidator;

import java.util.Set;

public class PartnerForm extends FormPanel {

    private FormBinding binding;

    private final TextField<String> nameField;

    public PartnerForm(Set<String> existingPartnerName) {
        super();

        binding = new FormBinding(this);

        UiConstants constants = GWT.create(UiConstants.class);

        nameField = new TextField<>();
        nameField.setFieldLabel(constants.name());
        nameField.setMaxLength(PartnerDTO.NAME_MAX_LENGTH);
        nameField.setAllowBlank(false);
        nameField.setValidator(new UniqueNameValidator(existingPartnerName));
        binding.addFieldBinding(new FieldBinding(nameField, "name"));
        this.add(nameField);

        TextField<String> fullField = new TextField<>();
        fullField.setFieldLabel(constants.description());
        fullField.setMaxLength(64);
        binding.addFieldBinding(new FieldBinding(fullField, "fullName"));
        this.add(fullField);
    }

    public TextField<String> getNameField() {
        return nameField;
    }

    public FormBinding getBinding() {
        return binding;
    }
}
