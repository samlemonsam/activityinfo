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

import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.core.client.GWT;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.i18n.shared.UiConstants;

import org.activityinfo.legacy.shared.command.GetUsers;
import org.activityinfo.legacy.shared.command.result.UserResult;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.legacy.shared.model.UserPermissionDTO;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.page.entry.form.field.MultilineRenderer;


public class DatabaseTransferForm extends FormPanel {

    private PagingLoader<UserResult> loader;
    private ListStore<UserPermissionDTO> store;

    private final Dispatcher dispatcher;
    private UserDatabaseDTO database;
    private ComboBox<UserPermissionDTO> userField;

    private Text addUserWarning;
    private Text userInfo;

    public DatabaseTransferForm(UserDatabaseDTO database, Dispatcher dispatcher) {
        this.database = database;
        this.dispatcher = dispatcher;

        UiConstants constants = GWT.create(UiConstants.class);

        FormLayout layout = new FormLayout();
        layout.setLabelWidth(90);
        this.setLayout(layout);
        this.setScrollMode(Style.Scroll.AUTOY);

        loader = new BasePagingLoader<>(new UserPermissionProxy());
        loader.setRemoteSort(true);

        store = new ListStore<>(loader);
        store.setKeyProvider(userPermissionDTO -> userPermissionDTO.getEmail());

        userInfo = new Text(I18N.CONSTANTS.transferDatabaseUserInfo());
        this.add(userInfo);

        Html spacer = new Html();
        spacer.setHtml(SafeHtmlUtils.fromSafeConstant("<br>"));
        this.add(spacer);

        userField = new ComboBox<>();
        userField.setName("user");
        userField.setFieldLabel(constants.newDatabaseOwner());
        userField.setDisplayField("name");
        userField.setAllowBlank(false);
        userField.setStore(store);
        userField.setForceSelection(true);
        userField.setAllowBlank(false);
        userField.setItemRenderer(new MultilineRenderer<>(new ModelPropertyRenderer<>("name")));
        this.add(userField);

        addUserWarning = new Text(I18N.CONSTANTS.addUserBeforeTransferWarning());
        addUserWarning.setVisible(false);
        addUserWarning.setStyleAttribute("color", "red");
        this.add(addUserWarning);

        doLayout();
    }

    public UserPermissionDTO getUser() {
        return userField.getSelection().get(0);
    }

    private class UserPermissionProxy extends RpcProxy<UserResult> {

        @Override
        protected void load(Object loadConfig, final AsyncCallback<UserResult> callback) {

            GetUsers command = new GetUsers(database.getId());
            command.setSortInfo(new SortInfo("name", Style.SortDir.ASC));

            dispatcher.execute(command, new AsyncCallback<UserResult>() {
                @Override
                public void onFailure(Throwable caught) {
                    userField.disable();
                    callback.onFailure(caught);
                }

                @Override
                public void onSuccess(UserResult result) {
                    if (result.getData().isEmpty()) {
                        userField.disable();
                        addUserWarning.setVisible(true);
                    } else {
                        userField.enable();
                        addUserWarning.setVisible(false);
                    }
                    callback.onSuccess(result);
                }
            });
        }
    }

}
