package org.activityinfo.ui.client.page.config.design.dialog;
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.watopi.chosen.client.event.ChosenChangeEvent;
import com.watopi.chosen.client.gwt.ChosenListBox;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.client.callback.SuccessCallback;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.ui.client.util.GwtUtil;
import org.activityinfo.ui.client.widget.dialog.WizardDialog;
import org.activityinfo.ui.client.widget.dialog.WizardPageAdapter;
import org.activityinfo.ui.client.widget.form.FormGroup;

import java.util.List;

/**
 * @author yuriyz on 11/13/2014.
 */
public class NewDbExistingListPage extends WizardPageAdapter {

    private static OurUiBinder uiBinder = GWT.create(OurUiBinder.class);

    interface OurUiBinder extends UiBinder<Widget, NewDbExistingListPage> {
    }

    private final Dispatcher dispatcher;
    private final NewDbDialogData dialogData;
    private final Widget rootPanel;

    private List<UserDatabaseDTO> databaseList;

    @UiField
    ChosenListBox list;
    @UiField
    FormGroup dbField;

    public NewDbExistingListPage(Dispatcher dispatcher, NewDbDialogData dialogData) {
        this.dispatcher = dispatcher;
        this.dialogData = dialogData;
        this.rootPanel = uiBinder.createAndBindUi(this);

        list.addChosenChangeHandler(new ChosenChangeEvent.ChosenChangeHandler() {
            @Override
            public void onChange(ChosenChangeEvent event) {
                fireValidation();
            }
        });

        loadDatabaseList();
    }

    private void loadDatabaseList() {
        dispatcher.execute(new GetSchema(), new SuccessCallback<SchemaDTO>() {
            @Override
            public void onSuccess(SchemaDTO result) {
                databaseList = result.getDatabases();
                for (UserDatabaseDTO db : databaseList) {
                    list.addItem(db.getName(), Integer.toString(db.getId()));
                }
            }
        });
    }

    @Override
    public boolean isValid() {
        dbField.showValidationMessage(false);

        boolean isValid = GwtUtil.isInt(list.getValue());
        if (isValid) {
            updateDialogData();
        } else {
            dbField.showValidationMessage(true);
        }
        return isValid;
    }

    private void updateDialogData() {
        UserDatabaseDTO db = getSelectedDb();
        dialogData.setSourceDatabaseCountryId(db.getCountry().getId());
        dialogData.getCommand().setSourceDatabaseId(db.getId());
        dialogData.setHasDesignPrivileges(db.isDesignAllowed());
    }

    @Override
    public void onShow(WizardDialog wizardDialog) {
        super.onShow(wizardDialog);
        wizardDialog.getDialog().getPrimaryButton().setText(I18N.CONSTANTS.nextButton());
    }

    private UserDatabaseDTO getSelectedDb() {
        int dbId = GwtUtil.getIntSilently(list.getValue());
        for (UserDatabaseDTO db : databaseList) {
            if (db.getId() == dbId) {
                return db;
            }
        }
        throw new RuntimeException("Unable to find db with id: " + dbId);
    }

    @Override
    public IsWidget asWidget() {
        return rootPanel;
    }

}

