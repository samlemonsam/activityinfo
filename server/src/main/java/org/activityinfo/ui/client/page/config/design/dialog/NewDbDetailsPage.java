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

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.watopi.chosen.client.event.ChosenChangeEvent;
import com.watopi.chosen.client.gwt.ChosenListBox;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.shared.command.GetCountries;
import org.activityinfo.legacy.shared.command.result.CountryResult;
import org.activityinfo.legacy.shared.model.CountryDTO;
import org.activityinfo.ui.client.util.GwtUtil;
import org.activityinfo.ui.client.widget.Button;
import org.activityinfo.ui.client.widget.CheckBox;
import org.activityinfo.ui.client.widget.TextBox;
import org.activityinfo.ui.client.widget.dialog.WizardDialog;
import org.activityinfo.ui.client.widget.dialog.WizardPageAdapter;
import org.activityinfo.ui.client.widget.form.FormGroup;

/**
 * @author yuriyz on 11/13/2014.
 */
public class NewDbDetailsPage extends WizardPageAdapter {

    private static OurUiBinder uiBinder = GWT.create(OurUiBinder.class);

    interface OurUiBinder extends UiBinder<Widget, NewDbDetailsPage> {
    }

    private final Dispatcher dispatcher;
    private final Widget rootPanel;
    private final NewDbDialogData dialogData;

    @UiField
    HTMLPanel optionsContainer;
    @UiField
    CheckBox copyUser;
    @UiField
    CheckBox copyPartners;
    @UiField
    CheckBox copyData;
    @UiField
    FormGroup nameField;
    @UiField
    FormGroup descriptionField;
    @UiField
    FormGroup countryField;
    @UiField
    ChosenListBox country;
    @UiField
    TextBox name;
    @UiField
    TextBox description;
    @UiField
    Button loadCountries;

    public NewDbDetailsPage(Dispatcher dispatcher, NewDbDialogData dialogData) {
        this.dispatcher = dispatcher;
        this.dialogData = dialogData;
        this.rootPanel = uiBinder.createAndBindUi(this);

        loadCountries();

        name.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                fireValidation();
            }
        });
        description.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                fireValidation();
            }
        });
        country.addChosenChangeHandler(new ChosenChangeEvent.ChosenChangeHandler() {
            @Override
            public void onChange(ChosenChangeEvent event) {
                setCopyDataCheckboxState();
                fireValidation();
            }
        });
        copyUser.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (copyUser.getValue() && !copyPartners.getValue()) {
                    copyPartners.setValue(true);
                }
            }
        });
        loadCountries.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                loadCountries();
            }
        });
    }

    private void loadCountries() {
        loadCountries.setEnabled(false);
        countryField.setShowValidationMessage(false);

        dispatcher.execute(new GetCountries(), new AsyncCallback<CountryResult>() {
            @Override
            public void onFailure(Throwable caught) {
                loadCountries.setEnabled(true);
                countryField.showValidationMessage(I18N.CONSTANTS.failedToLoadCountries());
                loadCountries.setVisible(true);
            }

            @Override
            public void onSuccess(CountryResult result) {
                loadCountries.setVisible(false);

                for (CountryDTO countryDTO : result.getData()) {
                    country.addItem(countryDTO.getName(), Integer.toString(countryDTO.getId()));
                }
                country.update();
            }
        });
    }

    @Override
    public boolean isValid() {
        boolean result = true;

        nameField.showValidationMessage(false);
        descriptionField.showValidationMessage(false);
        countryField.showValidationMessage(false);

        if (Strings.isNullOrEmpty(name.getValue())) {
            nameField.showValidationMessage(true);
            result = false;
        }
        // description is not required
//        if (Strings.isNullOrEmpty(description.getValue())) {
//            descriptionField.showValidationMessage(true);
//            result = false;
//        }
        if (Strings.isNullOrEmpty(country.getValue()) || !GwtUtil.isInt(country.getValue())) {
            countryField.showValidationMessage(true);
            result = false;
        }

        if (result) {
            updateCommand();
        }

        return result;
    }

    private void updateCommand() {
        dialogData.getCommand().setName(name.getValue());
        dialogData.getCommand().setDescription(description.getValue());
        dialogData.getCommand().setCountryId(GwtUtil.getIntSilently(country.getValue()));
        dialogData.getCommand().setCopyData(copyData.getValue());
        dialogData.getCommand().setCopyPartners(copyPartners.getValue());
        dialogData.getCommand().setCopyUserPermissions(copyUser.getValue());
    }

    public NewDbDetailsPage showCopyOptions(boolean show) {
        optionsContainer.setVisible(show);
        return this;
    }

    @Override
    public void onShow(WizardDialog wizardDialog) {
        super.onShow(wizardDialog);

        boolean isCountrySet = dialogData.getCommand().getCountryId() > 0;
        if (isCountrySet) {
            setCopyDataCheckboxState();
        }
        copyUser.setEnabled(dialogData.hasDesignPrivileges());
        wizardDialog.getDialog().getPrimaryButton().setText(I18N.CONSTANTS.create());

    }

    private void setCopyDataCheckboxState() {
        boolean sameCountry = dialogData.getSourceDatabaseCountryId() == GwtUtil.getIntSilently(country.getValue());
        copyData.setEnabled(sameCountry);
        if (!sameCountry) {
            copyData.setValue(false);
        }
    }

    @Override
    public IsWidget asWidget() {
        return rootPanel;
    }
}
