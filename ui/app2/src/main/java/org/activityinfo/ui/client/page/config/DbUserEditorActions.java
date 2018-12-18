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
package org.activityinfo.ui.client.page.config;

import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Record;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.command.BatchCommand;
import org.activityinfo.legacy.shared.command.UpdateUserPermissions;
import org.activityinfo.legacy.shared.command.result.BatchResult;
import org.activityinfo.legacy.shared.command.result.BillingException;
import org.activityinfo.legacy.shared.command.result.UserExistsException;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.legacy.shared.model.UserPermissionDTO;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.dispatch.monitor.MaskingAsyncMonitor;
import org.activityinfo.ui.client.page.NavigationCallback;
import org.activityinfo.ui.client.page.common.dialog.FormDialogCallback;
import org.activityinfo.ui.client.page.common.dialog.FormDialogImpl;
import org.activityinfo.ui.client.page.config.form.FolderAssignmentException;
import org.activityinfo.ui.client.page.config.form.PermissionAssignmentException;
import org.activityinfo.ui.client.page.config.form.UserForm;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DbUserEditorActions {

    private static final Logger LOGGER = Logger.getLogger(DbUserEditorActions.class.getName());

    private DbUserEditor panel;
    private Dispatcher dispatcher;
    private PagingLoader loader;
    private ListStore<UserPermissionDTO> store;
    private Grid<UserPermissionDTO> grid;
    private UserDatabaseDTO db;

    public DbUserEditorActions(DbUserEditor panel, Dispatcher dispatcher, PagingLoader loader, ListStore<UserPermissionDTO> store, Grid<UserPermissionDTO> grid, UserDatabaseDTO db) {
        this.panel = panel;
        this.dispatcher = dispatcher;
        this.loader = loader;
        this.store = store;
        this.grid = grid;
        this.db = db;
    }

    public void save() {
        save(null);
    }

    public void save(final NavigationCallback callback) {
        BatchCommand batch = new BatchCommand();
        for (Record record : store.getModifiedRecords()) {
            batch.add(new UpdateUserPermissions(db.getId(), (UserPermissionDTO) record.getModel()));
        }

        dispatcher.execute(batch,
                new MaskingAsyncMonitor(panel, I18N.CONSTANTS.saving()),
                new AsyncCallback<BatchResult>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        // handled by monitor
                        if (callback != null) {
                            callback.onDecided(false);
                        }
                    }

                    @Override
                    public void onSuccess(BatchResult result) {
                        store.commitChanges();
                        panel.setModified(false);
                        if (callback != null) {
                            callback.onDecided(true);
                        }
                    }
                });
    }

    public void add() {
        final UserForm form = new UserForm(db);
        panel.setModified(true);
        showDialog(form, true);
    }

    public void edit(UserPermissionDTO user) {
        final UserForm form = new UserForm(db);
        form.edit(user);
        panel.setModified(true);
        showDialog(form, false);
    }

    private void showDialog(final UserForm form, final boolean newUser) {
        final FormDialogImpl dlg = new FormDialogImpl(form);
        dlg.setHeadingText(newUser ? I18N.CONSTANTS.newUser() : I18N.CONSTANTS.editUser());
        dlg.setWidth(400);
        dlg.setHeight(300);
        dlg.getCancelButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                panel.setModified(false);
            }
        });

        final String host = Window.Location.getHostName();

        dlg.show(new FormDialogCallback() {

            @Override
            public void onValidated() {
                try {
                    UpdateUserPermissions command = new UpdateUserPermissions(db, form.getUser(), host);
                    command.setNewUser(newUser);
                    dispatcher.execute(command,
                            new AsyncCallback<VoidResult>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    if (caught instanceof UserExistsException) {
                                        MessageBox.alert(I18N.CONSTANTS.userExistsTitle(), I18N.CONSTANTS.userExistsMessage(), null);
                                    } else if(caught instanceof BillingException) {
                                        showBillingProblem((BillingException) caught);
                                    } else {
                                        LOGGER.log(Level.SEVERE, "Add user failed", caught);
                                        MessageBox.alert(I18N.CONSTANTS.serverError(), I18N.CONSTANTS.errorUnexpectedOccured(), null);
                                    }
                                }

                                @Override
                                public void onSuccess(VoidResult result) {
                                    loader.load();
                                    panel.setModified(false);
                                    dlg.hide();
                                }
                            });
                } catch (FolderAssignmentException excp) {
                    MessageBox.alert(I18N.CONSTANTS.noFolderAssignmentTitle(), excp.getMessage(), null);
                } catch (PermissionAssignmentException excp) {
                    MessageBox.alert(I18N.CONSTANTS.permissionAssignmentErrorTitle(), excp.getMessage(), null);
                }
            }
        });
    }

    private void showBillingProblem(BillingException billingException) {
        if(billingException.isTrial()) {
            MessageBox.alert(SafeHtmlUtils.fromString(I18N.CONSTANTS.usersExceeded()),
                    SafeHtmlUtils.fromTrustedString("During your free trial, you may not add more than " +
                            billingException.getUserLimit() + " unique users across all of your databases." +
                            "Please <a href=\"mailto:info@activityinfo.org\">contact us</a> to set up billing."), null);
        } else {
            MessageBox.alert(SafeHtmlUtils.fromString(I18N.CONSTANTS.usersExceeded()),
                    SafeHtmlUtils.fromTrustedString("You cannot add this user without exceeding the user limits " +
                            "(" + billingException.getUserLimit()+ " users) of the " +
                            "\"" + billingException.getBillingAccountName() + "\". " +
                            "Please <a href=\"mailto:info@activityinfo.org\">contact us</a> to increase your plan."), null);
        }
    }

    public void delete() {
        panel.setModified(true);
        final UserPermissionDTO model = grid.getSelectionModel().getSelectedItem();
        model.setAllowView(false);
        model.setAllowViewAll(false);
        model.setAllowEdit(false);
        model.setAllowEditAll(false);
        model.setAllowDesign(false);
        model.setAllowManageAllUsers(false);
        model.setAllowManageUsers(false);
        model.setAllowExport(false);

        MessageBox.confirm(
                I18N.CONSTANTS.removeUser(),
                I18N.MESSAGES.requestConfirmationToRemoveUser(model.getEmail()),
                event -> {
                    if(event.getButtonClicked().getItemId().equals(Dialog.YES)) {
                        delete(model);
                    } else {
                        panel.setModified(false);
                    }
                });
    }

    private void delete(UserPermissionDTO model) {
        dispatcher.execute(new UpdateUserPermissions(db.getId(), model),
                new MaskingAsyncMonitor(panel, I18N.CONSTANTS.deleting()),
                new AsyncCallback<VoidResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        panel.setModified(false);
                    }

                    @Override
                    public void onSuccess(VoidResult result) {
                        store.remove(model);
                        panel.setModified(false);
                    }
                });
    }

    public void export() {
        Window.open(GWT.getModuleBaseURL() + "export/users?dbUsers=" + db.getId(), "_blank", null);
    }
}
