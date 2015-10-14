package org.activityinfo.test.driver;

import com.google.common.collect.Lists;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.bootstrap.BsFormPanel;
import org.activityinfo.test.pageobject.bootstrap.BsModal;
import org.activityinfo.test.pageobject.web.components.Form;
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
        return Lists.newArrayList();
    }
}
