package org.activityinfo.test.pageobject.gxt;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.api.FluentElements;
import org.activityinfo.test.pageobject.web.components.Form;
import org.joda.time.LocalDate;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

import java.util.List;

import static org.activityinfo.test.pageobject.api.XPathBuilder.*;

public class GxtFormPanel extends Form {

    private FluentElement form;

    public GxtFormPanel(FluentElement form) {
        this.form = form;
    }

    private FluentElement findFormItemWithLabel(String labelText) {
        try {
            return form.find().label(withText(labelText + ":")).ancestor().div(withClass("x-form-item")).first();
        } catch(NoSuchElementException e) {
            throw new AssertionError(String.format("The form panel has no field with label %s", labelText));
        }
    }

    @Override
    public void fillTextField(String label, String value) {
        findFormItemWithLabel(label).findElement(By.tagName("input")).sendKeys(value);
    }

    @Override
    public void fillDateField(String label, LocalDate date) {
        fillTextField(label, date.toString("M/d/YY"));
    }

    @Override
    public void select(String label, String itemLabel) {
        FluentElement field = findFormItemWithLabel(label);
        
        field.findElement(By.className("x-form-trigger-arrow")).click();
        
        FluentElement list = field.waitFor(By.className("x-combo-list-inner"));
        
        
        FluentElements items = list.findElements(By.className("x-combo-list-item"));
        List<String> itemLabels = Lists.newArrayList();
        for (FluentElement element : items) {
            if(element.text().equals(itemLabel)) {
                element.click();
            }
            itemLabels.add(element.text());
        }

        // Report nice error message
        throw new AssertionError(String.format("Could not select '%s' from combo box '%s'. Options:\n%s", 
                itemLabel, 
                label,
                Joiner.on("\n").join(itemLabels)));
    }
}
