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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ModelPropertyRenderer;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.i18n.shared.UiConstants;
import org.activityinfo.legacy.shared.model.FolderDTO;
import org.activityinfo.legacy.shared.model.PartnerDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.legacy.shared.model.UserPermissionDTO;
import org.activityinfo.ui.client.page.config.design.BlankValidator;
import org.activityinfo.ui.client.page.entry.form.field.MultilineRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserForm extends FormPanel {

    private final CheckBox allFolderCheckbox;
    private final CheckBoxGroup permissionsGroup;

    interface Templates extends SafeHtmlTemplates {

        @Template("<span style=\"font-weight: bold; font-style: italic\">{0}</span>")
        SafeHtml allFoldersLabel(String label);
    }

    private static final Templates TEMPLATES = GWT.create(Templates.class);

    private UserDatabaseDTO database;
    private TextField<String> nameField;
    private TextField<String> emailField;
    private ComboBox<PartnerDTO> partnerCombo;
    private Map<Integer, CheckBox> folderCheckBoxMap = new HashMap<>();

    private Integer userId;

    public UserForm(UserDatabaseDTO database) {
        this.database = database;

        UiConstants constants = GWT.create(UiConstants.class);

        FormLayout layout = new FormLayout();
        layout.setLabelWidth(90);
        this.setLayout(layout);
        this.setScrollMode(Style.Scroll.AUTOY);

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
        partnerCombo.setItemRenderer(new MultilineRenderer<>(new ModelPropertyRenderer<>("name")));
        this.add(partnerCombo);

        permissionsGroup = new CheckBoxGroup();
        permissionsGroup.setFieldLabel(I18N.CONSTANTS.permissions());
        permissionsGroup.setOrientation(Style.Orientation.VERTICAL);
        permissionsGroup.add(permissionsCheckBox("allowView", I18N.CONSTANTS.allowView()));
        permissionsGroup.add(permissionsCheckBox("allowEdit", I18N.CONSTANTS.allowEdit()));
        permissionsGroup.add(permissionsCheckBox("allowViewAll", I18N.CONSTANTS.allowViewAll()));
        permissionsGroup.add(permissionsCheckBox("allowEditAll", I18N.CONSTANTS.allowEditAll()));
        permissionsGroup.add(permissionsCheckBox("allowManageUsers", I18N.CONSTANTS.allowManageUsers()));
        permissionsGroup.add(permissionsCheckBox("allowManageAllUsers", I18N.CONSTANTS.allowManageAllUsers()));
        permissionsGroup.add(permissionsCheckBox("allowDesign", I18N.CONSTANTS.allowDesign()));
        this.add(permissionsGroup);

        CheckBoxGroup folderGroup = new CheckBoxGroup();
        folderGroup.setFieldLabel(I18N.CONSTANTS.folders());
        folderGroup.setOrientation(Style.Orientation.VERTICAL);

        allFolderCheckbox = new CheckBox();
        allFolderCheckbox.setBoxLabel(TEMPLATES.allFoldersLabel(I18N.CONSTANTS.all()));
        allFolderCheckbox.addListener(Events.Change, this::onAllFoldersChanged);
        folderGroup.add(allFolderCheckbox);

        for (FolderDTO folder : database.getFolders()) {
            CheckBox folderCheckBox = new CheckBox();
            folderCheckBox.setBoxLabel(folder.getName());
            folderGroup.add(folderCheckBox);
            folderCheckBoxMap.put(folder.getId(), folderCheckBox);
        }
        this.add(folderGroup);

        if(!database.isManageAllUsersAllowed()) {
            partnerCombo.setValue(database.getMyPartner());
            partnerCombo.setReadOnly(true);
        }
    }

    private Field<?> permissionsCheckBox(String name, String label) {
        CheckBox checkBox = new CheckBox();
        boolean hasPermission = database.isAllowed(name, null);

        checkBox.setBoxLabel(label);
        checkBox.setName(name);
        checkBox.setValue(hasPermission);
        checkBox.setEnabled(hasPermission);

        return checkBox;
    }

    private void onAllFoldersChanged(BaseEvent baseEvent) {
        for (CheckBox checkBox : folderCheckBoxMap.values()) {
            checkBox.setValue(allFolderCheckbox.getValue());
        }
    }


    public void edit(UserPermissionDTO user) {

        emailField.setValue(user.getEmail());
        emailField.setReadOnly(true);

        nameField.setValue(user.getName());
        nameField.setReadOnly(true);

        partnerCombo.setValue(user.getPartner());
        partnerCombo.setReadOnly(true);

        addEditPermissionsGroup(user);
        addEditFolderPermissions(user);
    }

    private void addEditPermissionsGroup(UserPermissionDTO user) {
        for (Field<?> field : permissionsGroup.getAll()) {
            CheckBox checkBox = (CheckBox) field;

            String permissionName = checkBox.getName();
            Boolean allowed = user.get(permissionName);
            checkBox.setValue(allowed == Boolean.TRUE);

            checkBox.setEnabled(database.isAllowed(permissionName, user));
        }
    }

    private void addEditFolderPermissions(UserPermissionDTO user) {
        // set the value of overlapping folders
        if (user.hasFolderLimitation()) {
            user.getFolders().forEach(folder -> {
                CheckBox checkBox = folderCheckBoxMap.get(folder.getId());
                if (checkBox != null) {
                    checkBox.setValue(true);
                }
            });
        } else {
            allFolderCheckbox.setValue(true);
        }

        // enable/disable checkboxes depending on permissions
        allFolderCheckbox.setEnabled(database.isManageUsersAllowed(user));
        folderCheckBoxMap.forEach((i,checkBox) -> {
            checkBox.setEnabled(database.isManageUsersAllowed(user));
        });
    }

    public UserPermissionDTO getUser() {
        UserPermissionDTO user = new UserPermissionDTO();
        user.setEmail(emailField.getValue());
        user.setName(nameField.getValue());
        user.setPartner(partnerCombo.getValue());

        for (CheckBox checkBox : permissionsGroup.getValues()) {
            user.set(checkBox.getName(), checkBox.getValue());
        }

        if(allFolderCheckbox.getValue() != Boolean.TRUE || database.hasFolderLimitation()) {
            List<FolderDTO> folders = new ArrayList<>();
            for (Map.Entry<Integer, CheckBox> entry : folderCheckBoxMap.entrySet()) {
                CheckBox checkBox = entry.getValue();
                if(checkBox.getValue() == Boolean.TRUE) {
                    FolderDTO folder = new FolderDTO();
                    folder.setId(entry.getKey());
                    folders.add(folder);
                }
            }
            user.setFolders(folders);
        }

        return user;
    }
}
