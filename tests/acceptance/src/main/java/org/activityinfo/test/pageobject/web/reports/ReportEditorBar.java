package org.activityinfo.test.pageobject.web.reports;

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.api.XPathBuilder;
import org.openqa.selenium.Keys;

import static org.activityinfo.test.pageobject.api.XPathBuilder.*;

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
        waitForSavedNotification();
    }
    
    public void pinToDashboard() {
        container.find().button(containingText(I18N.CONSTANTS.pinToDashboard())).first().click();
        waitForSavedNotification();
    }
    
    public void waitForSavedNotification() {
        container.find().span(withClass("x-info-header-text"), withText(I18N.CONSTANTS.saved())).waitForFirst();
    }
}
