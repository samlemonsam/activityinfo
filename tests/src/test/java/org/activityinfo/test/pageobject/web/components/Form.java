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
package org.activityinfo.test.pageobject.web.components;

import org.activityinfo.test.pageobject.api.FluentElement;
import org.joda.time.LocalDate;

import java.util.List;

public abstract class Form {

    public void fillTextField(String label, String value) {
        findFieldByLabel(label).fill(value);
    }

    public void fillDateField(String label, LocalDate date) {
        findFieldByLabel(label).fill(date);
    }
    
    public void select(String label, String itemLabel) {
        findFieldByLabel(label).select(itemLabel);
    }
    
    public abstract FormItem findFieldByLabel(String label);

    public abstract boolean moveToNext();
    
    public abstract FormItem current();
    
    
    public static interface FormItem {
        
        String getLabel();
        String getPlaceholder();

        boolean isDropDown();
        boolean isSuggestBox();
        
        void fill(String value);
        void fill(LocalDate date);
        void select(String itemLabel);
        
        boolean isEnabled();

        FluentElement getElement();

        boolean isValid();

        List<String> availableItems();
    }
}
