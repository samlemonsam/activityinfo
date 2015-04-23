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
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.widget.dialog.WizardDialog;
import org.activityinfo.ui.client.widget.dialog.WizardPageAdapter;

/**
 * @author yuriyz on 11/13/2014.
 */
public class NewDbTypePage extends WizardPageAdapter {

    private static OurUiBinder uiBinder = GWT.create(OurUiBinder.class);

    interface OurUiBinder extends UiBinder<Widget, NewDbTypePage> {
    }

    private final Widget rootPanel;
    private final NewDbDialogData dialogData;

    @UiField
    RadioButton newDatabase;
    @UiField
    RadioButton copyDatabase;

    public NewDbTypePage(NewDbDialogData dialogData) {
        this.rootPanel = uiBinder.createAndBindUi(this);
        this.dialogData = dialogData;
    }

    @Override
    public void onShow(WizardDialog wizardDialog) {
        super.onShow(wizardDialog);
        wizardDialog.getDialog().getPrimaryButton().setText(I18N.CONSTANTS.nextButton());
    }

    @Override
    public void beforeLeave() {
        super.beforeLeave();
        dialogData.setCloneDb(copyDatabase.getValue());
    }

    @Override
    public IsWidget asWidget() {
        return rootPanel;
    }

    public boolean createNewDb() {
        return newDatabase.getValue();
    }
}
