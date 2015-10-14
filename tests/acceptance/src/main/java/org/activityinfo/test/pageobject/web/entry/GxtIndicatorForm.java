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
