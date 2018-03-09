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
package org.activityinfo.test.driver;

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.bootstrap.BsFormPanel;
import org.activityinfo.test.pageobject.bootstrap.BsModal;
import org.joda.time.LocalDate;

import java.util.List;

/**
 * Drives a bootstrap-based form
 */
public class BsDataEntryDriver implements DataEntryDriver {

    private final BsModal modal;
    private final BsFormPanel formPanel;

    
    public BsDataEntryDriver(FluentElement container) {
        this.modal = BsModal.find(container.root());
        this.formPanel = modal.form();
    }
    
    @Override
    public boolean nextField() {
        return formPanel.moveToNext();
    }

    @Override
    public void submit() throws InterruptedException {
        modal.click(I18N.CONSTANTS.save());
        modal.waitUntilClosed();
    }

    @Override
    public String getLabel() {
        return formPanel.current().getLabel();
    }

    @Override
    public void fill(String text) {
        formPanel.current().fill(text);
    }

    @Override
    public void fill(LocalDate date) {
        formPanel.current().fill(date);

    }

    @Override
    public void select(String itemLabel) {
        formPanel.current().select(itemLabel);
    }

    @Override
    public boolean isValid() {
        return formPanel.current().isValid();
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
        return formPanel.current().availableItems();
    }

    @Override
    public void close() {
        modal.cancel();
    }
}
