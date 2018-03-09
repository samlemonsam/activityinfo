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

}
