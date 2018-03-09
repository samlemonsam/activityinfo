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
import com.google.common.base.Stopwatch;
import org.activityinfo.test.driver.DataEntryDriver;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.api.FluentElements;
import org.activityinfo.test.pageobject.gxt.GxtFormPanel;
import org.activityinfo.test.pageobject.gxt.GxtMessageBox;
import org.activityinfo.test.pageobject.gxt.GxtModal;
import org.activityinfo.test.pageobject.web.components.Form;
import org.joda.time.LocalDate;
import org.openqa.selenium.By;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withText;


public class GxtFormDataEntryDriver implements DataEntryDriver {

    private GxtModal modal;
    private Iterator<FluentElement> sections;
    private Form currentForm;
    private Form.FormItem currentField;

    public GxtFormDataEntryDriver(GxtModal modal) {
        this.modal = modal;

        sections = this.modal.getWindowElement()
                .findElements(By.className("formSec"))
                .iterator();
    }

    @Override
    public List<String> availableValues() {
        if (currentField.isDropDown()) {
            return currentField.availableItems();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        modal.closeByWindowHeaderButton();
    }

    @Override
    public boolean nextField() {
        
        while(true) {
            if (currentForm == null || !currentForm.moveToNext()) {
                // end of this form page, move to next if possible
                if (!sections.hasNext()) {
                    return false;
                } else {
                    moveToNextSection();
                }
            } else {
                currentField = currentForm.current();
                if (currentField.isEnabled()) {
                    return true;
                }
                // otherwise see if next field is enabled
            }
        }
    }


    private void moveToNextSection() {
        FluentElement sectionElement = sections.next();
        sectionElement.click();
        String sectionName = sectionElement.findElement(By.className("formSecHeader")).text();
        
        System.out.println(sectionName);
        switch (sectionName) {
            case "Indicators":
                currentForm = new GxtIndicatorForm(modal.getWindowElement());
                break;
            case "Comments":
                currentForm = new GxtCommentsForm(modal.getWindowElement());
                break;
            default:
                currentForm = findVisibleForm();
                break;
        }
    }

    private GxtFormPanel findVisibleForm() {
        FluentElements forms = modal.getWindowElement().findElements(By.className("x-form-label-left"));
        for (FluentElement form : forms) {
            if(form.isDisplayed()) {
                return new GxtFormPanel(form);
            }
        }
        throw new IllegalStateException("No visible form panels");
    }

    @Override
    public String getLabel() {
        return currentField.getLabel();
    }

    @Override
    public void fill(String text) {
        if(currentField.isDropDown()) {
            currentField.select(text);
        } else {
            currentField.fill(text);
        }
    }

    @Override
    public void fill(LocalDate date) {
        currentField.fill(date);
    }

    @Override
    public void select(String itemLabel) {
        currentField.select(itemLabel);
    }

    @Override
    public boolean isValid() {
        return currentField.isValid();
    }

    @Override
    public boolean isNextEnabled() {
        return false;
    }

    @Override
    public void sendKeys(CharSequence keys) {
        currentField.getElement().sendKeys(keys);
    }

    @Override
    public void submit() throws InterruptedException {
        modal.getWindowElement().find().button(withText("Save")).clickWhenReady();

        Stopwatch stopwatch = Stopwatch.createStarted();
        while(stopwatch.elapsed(TimeUnit.SECONDS) < 15) {
            // Check for error
            Optional<GxtMessageBox> messageBox = GxtMessageBox.get(modal.getWindowElement());
            if(messageBox.isPresent()) {
                throw new AssertionError(messageBox.get().getMessage());
            }
            
            if(modal.isClosed()) {
                return;
            }
            
            Thread.sleep(150);
        }
    }
}
