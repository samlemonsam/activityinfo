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
package org.activityinfo.test.pageobject.web.reports;

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.gxt.Gxt;
import org.activityinfo.test.pageobject.gxt.GxtModal;
import org.openqa.selenium.Keys;

import static org.activityinfo.test.pageobject.api.XPathBuilder.containingText;
import static org.activityinfo.test.pageobject.api.XPathBuilder.withClass;

/**
 * Page object for the common report editor bar
 */
public class ReportEditorBar {
    
    private FluentElement container;

    public ReportEditorBar(FluentElement container) {
        this.container = container;
    }
    
    public void rename(String reportName) {
        container.find().span(containingText(I18N.CONSTANTS.changeTitle())).first().click();
        FluentElement input = container.find().div(withClass("x-editor")).input(withClass("x-form-text")).waitForFirst();
        input.sendKeys(reportName);
        input.sendKeys(Keys.ENTER);
    }
    
    public void save() {
        container.find().button(containingText(I18N.CONSTANTS.save())).first().click();
        Gxt.waitForSavedNotification(container);
    }
    
    public void pinToDashboard() {
        container.find().button(containingText(I18N.CONSTANTS.pinToDashboard())).first().click();
        Gxt.waitForSavedNotification(container);
    }
    
    public ShareReportsDialog share() {
        container.find().button(containingText(I18N.CONSTANTS.share())).first().click();
        GxtModal modal = GxtModal.waitForModal(container);
        return new ShareReportsDialog(modal);
    }
    
    
}
