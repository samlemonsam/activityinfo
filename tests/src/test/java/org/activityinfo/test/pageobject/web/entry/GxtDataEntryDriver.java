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

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.driver.DataEntryDriver;
import org.activityinfo.test.pageobject.gxt.GxtModal;
import org.joda.time.LocalDate;

import java.util.List;

public class GxtDataEntryDriver implements DataEntryDriver {
    
    private DataEntryDriver current;
    
    public GxtDataEntryDriver(GxtModal gxtModal) {
        if(gxtModal.getTitle().equals(I18N.CONSTANTS.chooseLocation())) {
            current = new LocationDataEntryDriver(new LocationDialog(gxtModal));
        } else {
            current = new GxtFormDataEntryDriver(gxtModal);
        }
    }
    
    public LocationDialog getLocationDialog() {
        if(!(current instanceof LocationDataEntryDriver)) {
            throw new AssertionError("The Location dialog is not displayed");
        }
        return ((LocationDataEntryDriver) current).getDialog();
    }

    public DataEntryDriver getCurrent() {
        return current;
    }

    @Override
    public boolean nextField() {
        return current.nextField();
    }

    @Override
    public void submit() throws InterruptedException {
        current.submit();
    }

    @Override
    public String getLabel() {
        return current.getLabel();
    }

    @Override
    public void fill(String text) {
        current.fill(text);
    }

    @Override
    public void fill(LocalDate date) {
        current.fill(date);
    }

    @Override
    public void select(String itemLabel) {
        current.fill(itemLabel);
    }

    @Override
    public boolean isValid() {
        return current.isValid();
    }

    @Override
    public boolean isNextEnabled() {
        return current.isNextEnabled();
    }

    @Override
    public void sendKeys(CharSequence keys) {
        current.sendKeys(keys);
    }

    @Override
    public List<String> availableValues() {
        return current.availableValues();
    }

    @Override
    public void close() {
        current.close();
    }
}
