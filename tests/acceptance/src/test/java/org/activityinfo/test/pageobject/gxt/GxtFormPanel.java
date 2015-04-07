package org.activityinfo.test.pageobject.gxt;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.api.FluentElements;
import org.activityinfo.test.pageobject.web.components.Form;
import org.joda.time.LocalDate;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

import java.util.List;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withClass;
import static org.activityinfo.test.pageobject.api.XPathBuilder.withText;

public class GxtFormPanel extends Form {

    private FluentElement form;
    private GxtField current;

    public GxtFormPanel(FluentElement form) {
        this.form = form;
    }

    
    public GxtField findFieldByLabel(String labelText) {
        try {
            return new GxtField(form.find().label(withText(labelText + ":")).ancestor().div(withClass("x-form-item")).first());
        } catch(NoSuchElementException e) {
            throw new AssertionError(String.format("The form panel has no field with label %s", labelText));
        }
    }

    @Override
    public FormItem current() {
        return current;
    }
    
    @Override
    public boolean moveToNext() {
        Optional<FluentElement> nextElement;
        if(current == null) {
            nextElement = form.find().div(withClass("x-form-item")).firstIfPresent();
        } else {
            nextElement = current.element.find().followingSibling().div(withClass("x-form-item")).firstIfPresent();
        }
        
        if(nextElement.isPresent()) {
            current = new GxtField(nextElement.get());
            System.out.println("current = " + current.getLabel());
            return true;
        } else {
            return false;
        }
    }

    public class GxtField implements FormItem {

        private final FluentElement element;

        public GxtField(FluentElement element) {
            this.element = element;
        }

        public String getLabel() {
            String label = this.element.find().label(withClass("x-form-item-label")).first().text();
            if(label.endsWith(":")) {
                return label.substring(0, label.length()-1);
            } else {
                return label;
            }
        }

        @Override
        public boolean isEnabled() {
            boolean enabled = !this.element.exists(By.className("x-form-readonly"));
            System.out.println(getLabel() + ".enabled = " + enabled);
            return enabled;
        }

        @Override
        public void fill(String value) {
            FluentElement input = element.findElement(By.tagName("input"));
            input.element().clear();
            input.sendKeys(value);
        }

        @Override
        public void fill(LocalDate date) {
            fill(date.toString("M/d/YY"));
        }

        @Override
        public void select(String itemLabel) {

            element.findElement(By.className("x-form-trigger-arrow")).click();

            FluentElement list = this.element.waitFor(By.className("x-combo-list-inner"));


            FluentElements items = list.findElements(By.className("x-combo-list-item"));
            List<String> itemLabels = Lists.newArrayList();
            for (FluentElement element : items) {
                String text = element.text();
                itemLabels.add(text);
                if(text.equals(itemLabel)) {
                    element.click();
                    return;
                }
            }

            // Report nice error message
            throw new AssertionError(String.format("Could not select '%s' from combo box '%s'. Options:\n%s",
                    itemLabel,
                    getLabel(),
                    Joiner.on("\n").join(itemLabels)));
        }
    }
}
