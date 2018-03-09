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
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.core.client.GWT;
import org.activityinfo.i18n.shared.UiConstants;
import org.activityinfo.ui.client.page.config.design.BlankValidator;

public class ProjectForm extends FormPanel {

    public static final int PROJECT_MAX_LENGTH = 255;

    private FormBinding binding;
    private final TextField<String> nameField;

    public ProjectForm() {
        super();

        binding = new FormBinding(this);

        UiConstants constants = GWT.create(UiConstants.class);

        nameField = new TextField<String>();
        nameField.setFieldLabel(constants.name());
        nameField.setMaxLength(PROJECT_MAX_LENGTH);
        nameField.setAllowBlank(false);
        nameField.setValidator(new BlankValidator());
        binding.addFieldBinding(new FieldBinding(nameField, "name"));
        this.add(nameField);

        TextArea descriptionTextArea = new TextArea();
        descriptionTextArea.setFieldLabel(constants.description());
        descriptionTextArea.setMaxLength(250);
        binding.addFieldBinding(new FieldBinding(descriptionTextArea, "description"));
        this.add(descriptionTextArea);
    }

    public TextField<String> getNameField() {
        return nameField;
    }

    public FormBinding getBinding() {
        return binding;
    }
}
