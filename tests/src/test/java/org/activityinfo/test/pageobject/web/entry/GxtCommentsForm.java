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

import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.web.components.Form;
import org.joda.time.LocalDate;

import java.util.List;


public class GxtCommentsForm extends Form {

    public static final String LABEL = "Comments";
    
    private final FluentElement textArea;
    private FormItem current = null;

    public GxtCommentsForm(FluentElement textArea) {
        this.textArea = textArea;
    }


    @Override
    public boolean moveToNext() {
        if(current == null) {
            current = new CommentField();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public FormItem current() {
        return current;
    }

    @Override
    public FormItem findFieldByLabel(String label) {
        if(label.equals(LABEL)) {
            return new CommentField();
        } else {
            throw new AssertionError("Comments form has only field with label " + LABEL);
        }
    }

    private class CommentField implements FormItem {

        @Override
        public String getLabel() {
            return LABEL;
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
            textArea.sendKeys(value);
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
        public boolean isEnabled() {
            return true;
        }

        @Override
        public FluentElement getElement() {
            throw new UnsupportedOperationException();
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
