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
package org.activityinfo.ui.client.page.config.design.dialog;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.legacy.shared.command.CreateEntity;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.dispatch.callback.SuccessCallback;
import org.activityinfo.ui.client.widget.dialog.WizardDialog;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yuriyz on 11/13/2014.
 */
public class NewDbDialog {

    private final Dispatcher dispatcher;
    private final WizardDialog dialog;
    private final NewDbDialogData dialogData = new NewDbDialogData();

    private SuccessCallback<Void> successCallback;

    public NewDbDialog(final Dispatcher dispatcher) {
        this.dispatcher = dispatcher;

        this.dialog = new WizardDialog(new NewDbPageSwitcher(dispatcher, dialogData));
        this.dialog.setTitle(I18N.CONSTANTS.createNewDatabase());
        this.dialog.setOnFinishHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                createDatabase();
            }
        });
    }

    private void createDatabase() {
        dialog.getDialog().getPrimaryButton().setText(I18N.CONSTANTS.creating());
        dialog.getDialog().disablePrimaryButton();

        dispatcher.execute(createCommand(), new AsyncCallback<CreateResult>() {
            @Override
            public void onFailure(Throwable caught) {
                dialog.getDialog().getPrimaryButton().setText(I18N.CONSTANTS.retry());
                dialog.getDialog().enablePrimaryButton();
                dialog.getDialog().getStatusLabel().setText(I18N.CONSTANTS.failedToCreateDatabase());
            }

            @Override
            public void onSuccess(CreateResult result) {
                dialog.hide();
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        if (successCallback != null) {
                            successCallback.onSuccess(null);
                        }
                    }
                });
            }
        });
    }

    private Command<CreateResult> createCommand() {
        if (dialogData.isCloneDb()) {
            return dialogData.getCommand();
        } else {
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put("name", dialogData.getCommand().getName());
            properties.put("description", dialogData.getCommand().getDescription());
            properties.put("countryId", dialogData.getCommand().getCountryId());
            return new CreateEntity("UserDatabase", properties);
        }
    }

    public NewDbDialog show() {
        dialog.show();
        return this;
    }

    public NewDbDialog setSuccessCallback(SuccessCallback<Void> successCallback) {
        this.successCallback = successCallback;
        return this;
    }
}
