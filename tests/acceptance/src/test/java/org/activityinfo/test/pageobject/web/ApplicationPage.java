package org.activityinfo.test.pageobject.web;

import org.activityinfo.test.pageobject.api.PageObject;
import org.activityinfo.test.pageobject.api.Path;
import org.activityinfo.test.pageobject.gxt.Gxt;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Interface to the single-pageobject application
 */
@Path("/")
public class ApplicationPage extends PageObject {

    @FindBy(xpath = "//div[text() = 'ActivityInfo']/following-sibling::div[2]")
    private WebElement settingsButton;

    /**
     * The outermost pageobject container
     */
    @FindBy(className = Gxt.BORDER_LAYOUT_CONTAINER)
    private WebElement pageContainer;


    public <T> T assertCurrentPageIs(Class<T> applicationPageClass) {
        return binder.create(pageContainer,  applicationPageClass);
    }
}
