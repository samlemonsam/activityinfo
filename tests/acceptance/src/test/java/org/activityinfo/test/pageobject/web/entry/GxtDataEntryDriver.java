package org.activityinfo.test.pageobject.web.entry;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import org.activityinfo.test.driver.DataEntryDriver;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.gxt.GxtFormPanel;
import org.activityinfo.test.pageobject.gxt.GxtMessageBox;
import org.activityinfo.test.pageobject.gxt.GxtModal;
import org.activityinfo.test.pageobject.web.components.Form;
import org.joda.time.LocalDate;
import org.openqa.selenium.By;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withText;


public class GxtDataEntryDriver implements DataEntryDriver {

    private GxtModal modal;
    private Iterator<FluentElement> sections;
    private Form currentForm;
    private Form.FormItem currentField;

    public GxtDataEntryDriver(GxtModal modal) {
        this.modal = modal;

        sections = this.modal.getWindowElement()
                .findElements(By.className("formSec"))
                .iterator();
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
        if(sectionName.equals("Indicators")) {
            currentForm = new GxtIndicatorForm(modal.getWindowElement());
        } else if(sectionName.equals("Comments")) {
            currentForm = new GxtCommentsForm(modal.getWindowElement());
        } else {
            currentForm = new GxtFormPanel(modal.getWindowElement().findElement(By.className("x-form-label-left")));
        }
    }

    @Override
    public String getLabel() {
        return currentField.getLabel();
    }

    @Override
    public void fill(String text) {
        currentField.fill(text);
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
    public boolean isNextEnabled() {
        return false;
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
