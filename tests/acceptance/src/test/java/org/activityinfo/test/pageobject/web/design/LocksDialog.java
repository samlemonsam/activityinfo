package org.activityinfo.test.pageobject.web.design;
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

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.gxt.GxtFormPanel;
import org.activityinfo.test.pageobject.gxt.GxtModal;
import org.joda.time.LocalDate;

import static org.activityinfo.test.pageobject.api.XPathBuilder.*;

/**
 * Wrap it with custom dialog instead of GxtModal because of custom layout.
 *
 * @author yuriyz on 06/10/2015.
 */
public class LocksDialog {

    private GxtModal modal;

    public LocksDialog(GxtModal modal) {
        this.modal = modal;
    }

    public LocksDialog name(String lockName) {
        modal.form().fillTextField(I18N.CONSTANTS.name(), lockName);
        return this;
    }

    public LocksDialog active(boolean lockActive) {
        if (lockActive) {
            modal.form().findFieldByLabel(I18N.CONSTANTS.enabledColumn()).getElement().clickWhenReady();
        }
        return this;
    }

    public LocksDialog startDate(LocalDate startDate) {
        modal.form().fillDateField(I18N.CONSTANTS.fromDate(), startDate);
        return this;
    }

    public LocksDialog endDate(LocalDate endDate) {
        modal.form().fillDateField(I18N.CONSTANTS.toDate(), endDate);
        return this;
    }

    public LocksDialog selectDatabase() {
        checkBoxByLabel(I18N.CONSTANTS.database()).clickWhenReady();
        return this;
    }

    public LocksDialog selectForm(String formName) {
        checkBoxByLabel(I18N.CONSTANTS.activity()).clickWhenReady();
        comboBox(I18N.CONSTANTS.activity()).select(formName);
        return this;
    }

    public LocksDialog selectProject(String projectName) {
        checkBoxByLabel(I18N.CONSTANTS.project()).clickWhenReady();
        comboBox(I18N.CONSTANTS.project()).select(projectName);
        return this;
    }

    private FluentElement checkBoxByLabel(String label) {
        return controlContainer(label).find().
                div(withClass("x-form-field")).input().first();
    }

    private GxtFormPanel.GxtField comboBox(String label) {
        return new GxtFormPanel.GxtField(controlContainer(label).find().div(withRole("combobox")).first());
    }

    private FluentElement controlContainer(String label) {
        return modal.getWindowElement().find().
                div(withClass("x-form-label"), withText(label)).
                ancestor().tr(withRole("presentation")).first();
    }

    public GxtModal getModal() {
        return modal;
    }
}
