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
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.ui.client.page.common.toolbar.WarningBar;
import org.activityinfo.ui.client.page.config.design.BlankValidator;
import org.activityinfo.ui.client.page.entry.form.field.MultilineRenderer;

import java.util.*;

/**

 +---------------------------------------------------------------------------------+
 | Edit User                                                                       |
 | +-----------------------------------------------------------------------------+ |
 | |                                                                             | |
 | | +-------------------------------------------------------------+           ^ | |
 | | |                                                    TextField|           | | |
 | | |              +--------------------------------------------+ |           | | |
 | | | Name:        |                                            | |           | | |
 | | |              +--------------------------------------------+ |           | | |
 | | |                                                             |           | | |
 | | +-------------------------------------------------------------+           | | |
 | |                                                                           | | |
 | | +-------------------------------------------------------------+           | | |
 | | |                                                    TextField|           | | |
 | | |              +--------------------------------------------+ |           | | |
 | | | Email:       |                                            | |           | | |
 | | |              +--------------------------------------------+ |           | | |
 | | |                                                             |           | | |
 | | +-------------------------------------------------------------+           | | |
 | |                                                                           | | |
 | | +-------------------------------------------------------------+           | | |
 | | |                                                     ComboBox|           | | |
 | | |              +--------------------------------------------+ |           | | |
 | | | Partner:     |                                            | |           | | |
 | | |              +--------------------------------------------+ |           | | |
 | | |                                                             |           | | |
 | | +-------------------------------------------------------------+           | | |
 | |                                                                           | | |
 | | +----------------------------------------------------------------------+  | | |
 | | |                                                         CheckBoxGroup|  | | |
 | | |                +----------------------+ +-------------------------+  |  | | |
 | | | Permissions:   |         CheckBoxGroup| |            CheckBoxGroup|  |  | | |
 | | |                | +--+                 | | +--+                    |  |  | | |
 | | |                | |  | View            | | |  | For All Partners   |  |  | | |
 | | |                | +--+                 | | +--+                    |  |  | | |
 | | |                |                      | |                         |  |  | | |
 | | |                |         .....        | |         .....           |  |  | | |
 | | |                |                      | |                         |  |  | | |
 | | |                | +--+                 | |                         |  |  | | |
 | | |                | |  | Design          | |                         |  |  | | |
 | | |                | +--+                 | |                         |  |  | | |
 | | |                |                      | |                         |  |  | | |
 | | |                +----------------------+ +-------------------------+  |  | | |
 | | |                                                                      |  | | |
 | | +----------------------------------------------------------------------+  | | |
 | |                                                                           | | |
 | | +---------------------------------------+                                 | | |
 | | |                          CheckBoxGroup|                                 | | |
 | | | Folders:          +--+                |                                 | | |
 | | |                   |  | All            |                                 | | |
 | | |                   +--+                |     +--+                        | | |
 | | |                                       |     |  | ---> CheckBox          | | |
 | | |                   +--+                |     +--+                        | | |
 | | |                   |  | Folder 1       |                                 | | |
 | | |                   +--+                |                                 | | |
 | | |                                       |                                 | | |
 | | |                   .....               |                                 | | |
 | | |                                       |                                 | | |
 | | +---------------------------------------+                                 v | |
 | |                                                                             | |
 | +-----------------------------------------------------------------------------+ |
 |                                                                                 |
 +---------------------------------------------------------------------------------+

 */
public class UserForm extends FormPanel {

    private final CheckBox allFolderCheckbox;
    private final CheckBoxGroup permissionsGroup;
    private final CheckBoxGroup operationsGroup;
    private final CheckBoxGroup allPartnersGroup;
    private final CheckBox viewCheckBox;
    private final CheckBox viewAllCheckBox;
    private CheckBoxGroup folderGroup = new CheckBoxGroup();
    private WarningBar permissionWarning  = new WarningBar();

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

    public UserForm(UserDatabaseDTO database) {
        this.database = database;

        UiConstants constants = GWT.create(UiConstants.class);

        FormLayout layout = new FormLayout();
        layout.setLabelWidth(90);
        this.setLayout(layout);
        this.setScrollMode(Style.Scroll.AUTOY);

        nameField = new TextField<>();
        nameField.setFieldLabel(constants.name());
        nameField.setAllowBlank(false);
        nameField.setValidator(new BlankValidator());
        nameField.setMaxLength(50);
        this.add(nameField);

        emailField = new TextField<>();
        emailField.setFieldLabel(constants.email());
        emailField.setAllowBlank(false);
        emailField.setRegex("\\S+@\\S+\\.\\S+");
        this.add(emailField);

        ListStore<PartnerDTO> partnerStore = new ListStore<>();
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
        permissionsGroup.setOrientation(Style.Orientation.HORIZONTAL);

        operationsGroup = new CheckBoxGroup();
        allPartnersGroup = new CheckBoxGroup();
        operationsGroup.setOrientation(Style.Orientation.VERTICAL);
        allPartnersGroup.setOrientation(Style.Orientation.VERTICAL);

        // View check box must always be set to true (and disabling it causes confusion), even if user wants to change it...
        viewCheckBox = permissionsCheckBox(PermissionType.VIEW, I18N.CONSTANTS.allowView());
        viewCheckBox.setValue(true);
        viewCheckBox.addListener(Events.Change, change -> viewCheckBox.setValue(true));

        viewAllCheckBox = permissionsCheckBox(PermissionType.VIEW_ALL, I18N.CONSTANTS.forAllPartners());

        operationsGroup.add(viewCheckBox);
        allPartnersGroup.add(viewAllCheckBox);

        addCheckBoxes(PermissionType.CREATE, PermissionType.CREATE_ALL, I18N.CONSTANTS.allowCreate(), true);
        addCheckBoxes(PermissionType.EDIT, PermissionType.EDIT_ALL, I18N.CONSTANTS.allowEdit(), true);
        addCheckBoxes(PermissionType.DELETE, PermissionType.DELETE_ALL, I18N.CONSTANTS.allowDelete(), true);
        addCheckBoxes(PermissionType.MANAGE_USERS, PermissionType.MANAGE_ALL_USERS, I18N.CONSTANTS.allowManageUsers(), false);
        operationsGroup.add(permissionsCheckBox(PermissionType.EXPORT_RECORDS, I18N.CONSTANTS.allowExport()));
        operationsGroup.add(permissionsCheckBox(PermissionType.DESIGN, I18N.CONSTANTS.allowDesign()));

        permissionsGroup.add(operationsGroup);
        permissionsGroup.add(allPartnersGroup);
        this.add(permissionsGroup);

        folderGroup.setFieldLabel(I18N.CONSTANTS.folders());
        folderGroup.setOrientation(Style.Orientation.VERTICAL);

        // If a database user has *assigned* folders, do not show All checkbox
        if (database.hasFolderLimitation()) {
            allFolderCheckbox = null;
        } else {
            allFolderCheckbox = new CheckBox();
            allFolderCheckbox.setValue(false);
            allFolderCheckbox.setBoxLabel(TEMPLATES.allFoldersLabel(I18N.CONSTANTS.all()));
            allFolderCheckbox.addListener(Events.Change, this::onAllFoldersChanged);
            folderGroup.add(allFolderCheckbox);
        }

        for (FolderDTO folder : database.getFolders()) {
            CheckBox folderCheckBox = new CheckBox();
            folderCheckBox.setBoxLabel(folder.getName());
            folderCheckBox.setValue(false);
            folderCheckBox.addListener(Events.Change, this::onIndividualFolderChanged);
            folderGroup.add(folderCheckBox);
            folderCheckBoxMap.put(folder.getId(), folderCheckBox);
        }
        this.add(folderGroup);

        if(!database.isManageAllUsersAllowed()) {
            partnerCombo.setValue(database.getMyPartner());
            partnerCombo.setReadOnly(true);
        }

        permissionWarning.setWarning(I18N.CONSTANTS.permissionEditingLockedWarning());
        permissionWarning.hide();
        this.add(permissionWarning);
    }

    private void addCheckBoxes(PermissionType permissionType, PermissionType partnerType, String label, boolean enforceViewAll) {
        CheckBox permissionCheckBox = permissionsCheckBox(permissionType, label);
        CheckBox partnerCheckBox = permissionsCheckBox(partnerType, I18N.CONSTANTS.forAllPartners());

        // If we haven't selected the permission, then the "All Partners" option is disabled
        permissionCheckBox.addListener(Events.Change, change -> allPartnersToggle(permissionCheckBox, partnerCheckBox));

        // If the "All Partners" option depends on the user also having VIEW_ALL permissions, then set it when selected
        if (enforceViewAll) {
            partnerCheckBox.addListener(Events.Change, change -> viewAllToggle(partnerCheckBox));
        }

        operationsGroup.add(permissionCheckBox);
        allPartnersGroup.add(partnerCheckBox);
    }

    private void allPartnersToggle(CheckBox permissionCheckBox, CheckBox partnerCheckBox) {
        if (permissionCheckBox.getValue() == Boolean.FALSE) {
            partnerCheckBox.setValue(false);
            partnerCheckBox.setEnabled(false);
        } else {
            partnerCheckBox.setEnabled(true);
        }
    }

    private void viewAllToggle(CheckBox partnerCheckBox) {
        if (partnerCheckBox.getValue() == Boolean.TRUE) {
            viewAllCheckBox.setValue(true);
        }
    }

    private CheckBox permissionsCheckBox(PermissionType permissionType, String label) {
        CheckBox checkBox = new CheckBox();
        boolean hasPermission = database.canGivePermission(permissionType, null);

        checkBox.setBoxLabel(label);
        checkBox.setName(permissionType.name());
        checkBox.setValue(hasPermission);
        checkBox.setEnabled(hasPermission);

        return checkBox;
    }

    private void onAllFoldersChanged(BaseEvent baseEvent) {
        for (CheckBox checkBox : folderCheckBoxMap.values()) {
            checkBox.setValue(allFolderCheckbox.getValue());
        }
    }

    private void onIndividualFolderChanged(BaseEvent baseEvent) {
        if (!(baseEvent.getSource() instanceof CheckBox)) {
            return;
        }

        CheckBox folderCheckBox = (CheckBox) baseEvent.getSource();
        if (folderCheckBox.getValue() == Boolean.FALSE && allFolderCheckbox.getValue() == Boolean.TRUE) {
            allFolderCheckbox.setFireChangeEventOnSetValue(false);
            allFolderCheckbox.setValue(false);
            allFolderCheckbox.setFireChangeEventOnSetValue(true);
        }
    }

    public void edit(UserPermissionDTO user) {

        emailField.setValue(user.getEmail());
        emailField.setReadOnly(true);

        nameField.setValue(user.getName());
        nameField.setReadOnly(true);

        partnerCombo.setValue(user.getPartner());
        partnerCombo.setReadOnly(!database.getAmOwner() && !database.isAllowed(PermissionType.MANAGE_ALL_USERS, user));

        addEditPermissionsGroup(user);
        addEditFolderPermissions(user);

        if (database.hasGreaterPermissions(user)) {
            permissionWarning.hide();
        } else {
            permissionWarning.show();
        }
    }

    private void addEditPermissionsGroup(UserPermissionDTO user) {
        for (Field<?> field : permissionsGroup.getAll()) {
            if (field instanceof CheckBoxGroup) {
                addEditPermissionsGroup((CheckBoxGroup) field, user);
            } else if (field instanceof CheckBox) {
                addEditPermissionsCheckBox((CheckBox) field, user);
            }
        }
    }

    private void addEditPermissionsGroup(CheckBoxGroup group, UserPermissionDTO user) {
        for (CheckBox checkBox : group.getValues()) {
            addEditPermissionsCheckBox(checkBox, user);
        }
    }

    private void addEditPermissionsCheckBox(CheckBox checkBox, UserPermissionDTO user) {
        PermissionType permissionType = PermissionType.valueOf(checkBox.getName());
        Boolean allowed = user.get(permissionType.getDtoPropertyName());
        checkBox.setValue(allowed == Boolean.TRUE);
        checkBox.setEnabled(database.canGivePermission(permissionType, user));
    }

    private void addEditFolderPermissions(UserPermissionDTO user) {
        // If both database user and user have access to all, just set the allFolderCheckbox to true
        if (!database.hasFolderLimitation() && !user.hasFolderLimitation()) {
            allFolderCheckbox.setValue(true);
        // Else add any of the user folders not yet included, and set the values of overlapping folders
        } else {
            addUserFolders(user);
        }

        // Set checkbox enabled status if database user has permission to assign a given folder and the database user
        // has an identical or greater set of permissions
        if (allFolderCheckbox != null) {
            allFolderCheckbox.setEnabled(database.hasGreaterPermissions(user));
        }
        folderCheckBoxMap.forEach((folderId, checkBox) -> {
            boolean enabled = database.canAssignFolder(folderId, user) && database.hasGreaterPermissions(user);
            checkBox.setEnabled(enabled);
        });
    }

    private void addUserFolders(UserPermissionDTO user) {
        user.getFolders().forEach(folder -> {
            // only add folder checkbox if not currently assigned to database user
            if (!folderCheckBoxMap.containsKey(folder.getId())) {
                CheckBox folderCheckBox = new CheckBox();
                folderCheckBox.setBoxLabel(folder.getName());
                folderCheckBox.setValue(true);
                folderCheckBoxMap.put(folder.getId(), folderCheckBox);
                folderGroup.add(folderCheckBox);
            // else set overlapping folder values to true
            } else {
                CheckBox folderCheckBox = folderCheckBoxMap.get(folder.getId());
                folderCheckBox.setValue(true);
            }
        });
    }

    public UserPermissionDTO getUser() throws PermissionAssignmentException, FolderAssignmentException {
        UserPermissionDTO user = new UserPermissionDTO();
        user.setEmail(emailField.getValue());
        user.setName(nameField.getValue());
        user.setPartner(partnerCombo.getValue());

        for (Field field : permissionsGroup.getAll()) {
            if (field instanceof CheckBoxGroup) {
                getPermissionFromCheckBoxGroup((CheckBoxGroup) field, user);
            } else if (field instanceof CheckBox) {
                getPermissionFromCheckBox((CheckBox) field, user);
            }
        }

        List<FolderDTO> folders = new ArrayList<>();

        // Check if current database user has All folder access, and has assigned it to the user
        if (allFolderCheckbox != null && allFolderCheckbox.getValue() == Boolean.TRUE) {
            user.setFolderLimitation(false);
            user.setFolders(database.getFolders());
            return user;
        }

        // Otherwise, we set the list of folders user is given access to
        folderCheckBoxMap.forEach((id,checkBox) -> {
            if (checkBox.getValue() == Boolean.TRUE) {
                FolderDTO folder = new FolderDTO();
                folder.setId(id);
                folders.add(folder);
            }
        });

        // Database user must select a folder/folders for user
        if (folders.isEmpty()) {
            throw new FolderAssignmentException(I18N.CONSTANTS.noFolderAssignmentMessage());
        }

        user.setFolderLimitation(true);
        user.setFolders(folders);
        return user;
    }

    private void getPermissionFromCheckBoxGroup(CheckBoxGroup checkBoxGroup, UserPermissionDTO user) {
        for (CheckBox checkBox : checkBoxGroup.getValues()) {
            getPermissionFromCheckBox(checkBox, user);
        }
    }

    private void getPermissionFromCheckBox(CheckBox checkBox, UserPermissionDTO user) {
        PermissionType permissionType = PermissionType.valueOf(checkBox.getName());
        Boolean value = checkBox.getValue();
        if (value == Boolean.TRUE && !database.isAllowed(permissionType, null)) {
            // if the database user does not have this permission - throw error
            throw new PermissionAssignmentException(I18N.CONSTANTS.permissionAssignmentErrorMessage());
        }
        user.set(permissionType.getDtoPropertyName(), checkBox.getValue());
    }
}
