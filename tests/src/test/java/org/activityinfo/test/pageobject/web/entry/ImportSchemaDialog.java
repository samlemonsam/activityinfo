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
package org.activityinfo.test.pageobject.web.entry;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.Sleep;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.bootstrap.BsModal;
import org.openqa.selenium.WebDriver;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withClass;
import static org.activityinfo.test.pageobject.api.XPathBuilder.withText;

/**
 * @author yuriyz on 10/02/2015.
 */
public class ImportSchemaDialog {

    private final BsModal modal;

    public ImportSchemaDialog(BsModal modal) {
        this.modal = modal;
    }

    public static ImportSchemaDialog waitOnDialog(FluentElement container) {
        return new ImportSchemaDialog(BsModal.find(container));
    }

    public ImportSchemaDialog enterCvsText(String cvsText) {
        FluentElement textArea = modal.form().getForm().find().textArea(withClass("form-control")).first();
        textArea.element().clear();
        textArea.sendKeys(cvsText);
        Sleep.sleepSeconds(1);
        return this;
    }

    public ImportSchemaDialog clickOk() {
        modal.click(I18N.CONSTANTS.ok());
        Sleep.sleepSeconds(1);
        return this;
    }

    public ImportSchemaDialog clickImportAnyway() {
        modal.click(I18N.CONSTANTS.ignoreImportWarnings());
        Sleep.sleepSeconds(1);
        return this;
    }

    public ImportSchemaDialog clickClose() {
        modal.click(I18N.CONSTANTS.close());
        Sleep.sleepSeconds(1);
        return this;
    }

    public void closeOnSuccess() {
        modal.getWindowElement().waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return isSuccessMessageShown();
            }
        });
        clickClose();
    }

    private boolean isSuccessMessageShown() {
        Optional<FluentElement> div = modal.getWindowElement().find().div(withText(I18N.CONSTANTS.databaseStructureSuccessfullyImported())).firstIfPresent();
        return div.isPresent() && div.get().isDisplayed();
    }
}
