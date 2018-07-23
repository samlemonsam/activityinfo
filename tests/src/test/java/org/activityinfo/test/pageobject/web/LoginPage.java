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
package org.activityinfo.test.pageobject.web;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.sut.Server;
import org.activityinfo.test.sut.UserAccount;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import javax.inject.Inject;
import java.net.URI;
import java.util.List;

public class LoginPage {
    public static final By EMAIL_INPUT = By.name("email");
    public static final By PASSWORD_INPUT = By.name("password");
    public static final By LOGIN_BUTTON = By.cssSelector("form > button");
    public static final By LOGIN_ALERT = By.className("alert--error");
    
    private final Server server;
    private final FluentElement page;

    @Inject
    public LoginPage(WebDriver webDriver, Server server) {
        this.page = new FluentElement(webDriver);
        this.server = server;
    }
    
    public LoginPage navigateTo() {
        page.navigate().to(server.path("login"));
        return this;
    }

    public LoginPage loginAs(UserAccount account) {
        page.waitFor(EMAIL_INPUT).sendKeys(account.getEmail());
        page.findElement(PASSWORD_INPUT).sendKeys(account.getPassword());
        page.findElement(LOGIN_BUTTON).clickWhenReady();
        return this;
    }

    public ApplicationPage andExpectSuccess() {
        page.waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                // Check for error messages
                List<WebElement> alerts = input.findElements(LOGIN_ALERT);
                try {
                    if (!alerts.isEmpty() && alerts.get(0).isDisplayed()) {
                        throw new RuntimeException("Login failed: " +
                                Iterables.getOnlyElement(alerts).getText());
                    }
                } catch (StaleElementReferenceException ignored) {}

                URI currentUri = page.getCurrentUri();

                if (currentUri.getPath().contains("unsupportedBrowser")) {
                    throw new RuntimeException("Unsupported browser");
                }

                return currentUri.getPath().equals("/app");
            }
        });
        
        return new ApplicationPage(page);
    }


    public void assertErrorMessageIsVisible() {
        page.waitUntil(ExpectedConditions.visibilityOfElementLocated(LOGIN_ALERT));
    }

    public void assertBrowserUnsupportedPageIsVisible() {
        page.waitUntil(ExpectedConditions.titleIs("Unsupported Browser"));
    }
}
