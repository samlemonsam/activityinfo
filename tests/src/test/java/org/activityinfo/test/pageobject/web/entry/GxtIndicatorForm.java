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

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.web.components.Form;
import org.joda.time.LocalDate;

import java.util.List;


public class GxtIndicatorForm extends Form {

    private FluentElement container;
    private IndicatorField current;

    public GxtIndicatorForm(FluentElement container) {
        this.container = container;
    }
    
    @Override
    public boolean moveToNext() {
        
        if(current == null) {
            Optional<FluentElement> first = container.find().input().ancestor().tr().firstIfPresent();
            if (first.isPresent()) {
                current = new IndicatorField(first.get());
                current.focus();
                return true;
            } else {
                return false;
            }
        } else {
            // otherwise use tab key to advance

            Optional<FluentElement> next = current.getElement().find().followingSibling().tr().firstIfPresent();
            if (next.isPresent()) {
                current = new IndicatorField(next.get());
                current.focus();
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public FormItem current() {
        return current;
    }

    @Override
    public FormItem findFieldByLabel(String label) {
        throw new UnsupportedOperationException();
    }
    
    
    private class IndicatorField implements FormItem {

        private final FluentElement row;

        public IndicatorField(FluentElement row) {
            this.row = row;
            assert row.getTagName().equals("tr");
        }

        public void focus() {
            row.find().input().clickWhenReady();
        }

        @Override
        public String getLabel() {
            FluentElement labelCell = row.find().td().first();
            String label = labelCell.text();
            assert !Strings.isNullOrEmpty(label);
            return label;
        }

        @Override
        public String getPlaceholder() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isDropDown() {
            return false;
        }

        @Override
        public boolean isSuggestBox() {
            return false;
        }

        @Override
        public void fill(String value) {
            FluentElement input = row.find().input().first();
            input.element().clear();
            input.sendKeys(value);
        }

        public String getName() {
            return row.find().input().first().attribute("name");
        }

        @Override
        public void fill(LocalDate date) {
            throw new UnsupportedOperationException("indicator fields cannot accept dates");
        }

        @Override
        public void select(String itemLabel) {
            throw new UnsupportedOperationException("indicator fields are not drop down lists");
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public FluentElement getElement() {
            return row;
        }

        @Override
        public boolean isValid() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<String> availableItems() {
            throw new UnsupportedOperationException();
        }
    }

}
