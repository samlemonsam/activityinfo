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
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.sut.Server;
import org.activityinfo.test.sut.UserAccount;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;

public class SignUpPage {


    public static final By NAME_INPUT = By.name("name");
    public static final By EMAIL_INPUT = By.name("email");
    public static final By TERMS_BUTTON = By.id("termsCheckbox");
    public static final By SUBMIT_BUTTON = By.xpath("//button[@type='submit']");


    private final Server server;
    private final FluentElement page;

    @Inject
    public SignUpPage(WebDriver webDriver, Server server) {
        this.page = new FluentElement(webDriver);
        this.server = server;
    }

    public SignUpPage navigateTo() {
        page.navigate().to(server.path("signUp"));
        return this;
    }

    public SignUpPage signUp(UserAccount account) {
        page.waitFor(NAME_INPUT).sendKeys(account.nameFromEmail());
        page.findElement(EMAIL_INPUT).sendKeys(account.getEmail());
        page.findElement(TERMS_BUTTON).click();
        page.findElement(SUBMIT_BUTTON).click();
        page.waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return input.getCurrentUrl().equals(server.path("signUp/sent"));
            }
        });
        return this;


    }

}



