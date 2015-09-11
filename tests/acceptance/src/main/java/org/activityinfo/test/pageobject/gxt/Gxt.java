package org.activityinfo.test.pageobject.gxt;


import com.google.common.base.Predicate;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withClass;
import static org.activityinfo.test.pageobject.api.XPathBuilder.withText;

public class Gxt {


    public static By borderContainer() {
        return By.xpath("div[contains(@class, 'x-border-panel')]");
    }

    public static final String BORDER_LAYOUT_CONTAINER = "x-border-layout-ct";

    public static final String COLUMN_LAYOUT_CONTAINER = "x-column-layout-ct";

    public static final String PORTLET = "x-portlet";
    
    public static final By WINDOW = By.className("x-window");
    
    public static By button(String label) {
        return By.xpath(String.format("//button[text() = '%s']", label));
    }

    public static void buttonClick(FluentElement element, String buttonName) {
        element.waitFor(button(buttonName)).clickWhenReady();
    }

    public static void waitUntilMaskDisappear(final FluentElement element) {
        element.waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver webDriver) {
                return !element.find().div(withClass("x-masked")).firstIfPresent().isPresent();
            }
        });
    }


    public static void waitForSavedNotification(FluentElement container) {
        container.find().span(withClass("x-info-header-text"), withText(I18N.CONSTANTS.saved())).waitForFirst();
    }

    public static void sleepSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
