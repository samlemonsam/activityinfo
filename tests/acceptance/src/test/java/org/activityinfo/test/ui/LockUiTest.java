package org.activityinfo.test.ui;
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

import net.lightoze.gwt.i18n.server.LocaleProxy;
import net.lightoze.gwt.i18n.server.ThreadLocalLocaleProvider;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.Sleep;
import org.activityinfo.test.driver.UiApplicationDriver;
import org.activityinfo.test.pageobject.gxt.GxtModal;
import org.activityinfo.test.pageobject.web.design.LocksPage;
import org.activityinfo.test.sut.DevServerAccounts;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Locale;

import static org.activityinfo.test.driver.Property.property;
import static org.junit.Assert.assertFalse;

/**
 * @author yuriyz on 09/18/2015.
 */
public class LockUiTest {

    private static final String DATABASE = "Locks";

    @Inject
    private UiApplicationDriver driver;

    @Inject
    private DevServerAccounts accounts;

    private void background() throws Exception {
        LocaleProxy.initialize();
        ThreadLocalLocaleProvider.pushLocale(Locale.forLanguageTag("en"));
        accounts.setLocale("en");

        driver.login();
        driver.setup().createDatabase(property("name", DATABASE));
    }

    @Test // AI-1091
    public void lockIsNotRemovedOnNoButtonClick() throws Exception {
        background();

        String lockName = "lockName";
        driver.addLockOnDb(lockName, DATABASE, "2015-01-01", "2015-01-01", true);

        LocksPage locksPage = locksPage();
        locksPage.grid().clickCell(lockName);
        GxtModal confirmDialog = locksPage.clickDelete();
        confirmDialog.clickButton(I18N.CONSTANTS.no());

        locksPage.grid().findCell(lockName); // assert lock is still in table
    }

    @Test // AI-1225
    public void deleteLock() throws Exception {
        background();

        String lockName = "lockName";
        driver.addLockOnDb(lockName, DATABASE, "2015-01-01", "2015-01-01", true);

        LocksPage locksPage = locksPage();
        locksPage.grid().clickCell(lockName);
        GxtModal confirmDialog = locksPage.clickDelete();
        confirmDialog.clickButton(I18N.CONSTANTS.yes());

        Sleep.sleepSeconds(1);

        assertFalse(locksPage.grid().findCellOptional(lockName).isPresent()); // assert lock is removed
        assertFalse(locksPage.getToolbarMenu().button(I18N.CONSTANTS.delete()).isEnabled()); // assert delete button is disabled
    }

    private LocksPage locksPage() {
        return driver.getApplicationPage().navigateToDesignTab().selectDatabase(driver.getAliasTable().getAlias(DATABASE)).locks();
    }


}
