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
