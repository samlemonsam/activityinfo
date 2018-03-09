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

import org.joda.time.LocalDate;

import java.util.List;

/**
 * Common interface to entering data through the web,
 * ODK, etc.
 */
public interface DataEntryDriver {


    /**
     * Navigates to the next field.
     * @return true if there is a next field, or false if the end of the 
     * form has been reached.
     */
    boolean nextField();

    /**
     * Submits the form
     * @return the new id of the form
     */
    void submit() throws InterruptedException;

    /**
     *
     * @return the label of the current data entry field
     */
    String getLabel();


    /**
     * 
     * Fills the current field with text
     */
    void fill(String text);
    
    void fill(LocalDate date);

    void select(String itemLabel);

    boolean isValid();


    /**
     *
     * @return true if it possible to navigate to the next field
     */
    boolean isNextEnabled();

    void sendKeys(CharSequence keys);

    List<String> availableValues();

    void close();
}
