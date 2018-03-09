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

import org.activityinfo.test.driver.DataEntryDriver;
import org.joda.time.LocalDate;

import java.util.List;

public class LocationDataEntryDriver implements DataEntryDriver {
    
    private final LocationDialog dialog;

    public LocationDataEntryDriver(LocationDialog dialog) {
        this.dialog = dialog;
    }

    public LocationDialog getDialog() {
        return dialog;
    }

    @Override
    public boolean nextField() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void submit() throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLabel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fill(String text) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fill(LocalDate date) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void select(String itemLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValid() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isNextEnabled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendKeys(CharSequence keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> availableValues() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        dialog.close();
    }
}
