package org.activityinfo.ui.client.page.config.form;

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

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ModelPropertyRenderer;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.core.client.GWT;
import org.activityinfo.i18n.shared.UiConstants;
import org.activityinfo.legacy.shared.model.PartnerDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.legacy.shared.model.UserPermissionDTO;
import org.activityinfo.ui.client.page.config.design.BlankValidator;
import org.activityinfo.ui.client.page.entry.form.field.MultilineRenderer;

public class UserForm extends FormPanel {

    private UserDatabaseDTO database;
    private TextField<String> nameField;
    private TextField<String> emailField;
    private ComboBox<PartnerDTO> partnerCombo;

    public UserForm(UserDatabaseDTO database) {
        this.database = database;

        UiConstants constants = GWT.create(UiConstants.class);

        FormLayout layout = new FormLayout();
        layout.setLabelWidth(90);
        this.setLayout(layout);

        nameField = new TextField<String>();
        nameField.setFieldLabel(constants.name());
        nameField.setAllowBlank(false);
        nameField.setValidator(new BlankValidator());
        nameField.setMaxLength(50);
        this.add(nameField);

        emailField = new TextField<String>();
        emailField.setFieldLabel(constants.email());
        emailField.setAllowBlank(false);
        emailField.setRegex("\\S+@\\S+\\.\\S+");
        this.add(emailField);

        ListStore<PartnerDTO> partnerStore = new ListStore<PartnerDTO>();
        partnerStore.add(database.getPartners());
        partnerStore.sort("name", SortDir.ASC);

        partnerCombo = new ComboBox<>();
        partnerCombo.setName("partner");
        partnerCombo.setFieldLabel(constants.partner());
        partnerCombo.setDisplayField("name");
        partnerCombo.setStore(partnerStore);
        partnerCombo.setForceSelection(true);
        partnerCombo.setTriggerAction(TriggerAction.ALL);
        partnerCombo.setAllowBlank(false);
        partnerCombo.setItemRenderer(new MultilineRenderer<>(new ModelPropertyRenderer<PartnerDTO>("name")));
        this.add(partnerCombo);

        if(!database.isManageAllUsersAllowed()) {
            partnerCombo.setValue(database.getMyPartner());
            partnerCombo.setReadOnly(true);
        }
    }

    protected CheckBox createCheckBox(String property, String label) {
        CheckBox box = new CheckBox();
        box.setName(property);
        box.setBoxLabel(label);
        return box;
    }

    public UserPermissionDTO getUser() {
        UserPermissionDTO user = new UserPermissionDTO();
        user.setEmail(emailField.getValue());
        user.setName(nameField.getValue());
        user.setPartner(partnerCombo.getValue());
        return user;
    }
}
