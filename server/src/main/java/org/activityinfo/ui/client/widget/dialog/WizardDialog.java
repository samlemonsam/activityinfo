package org.activityinfo.ui.client.widget.dialog;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import org.activityinfo.ui.client.widget.ModalDialog;

/**
 * @author yuriyz on 11/13/2014.
 */
public class WizardDialog {

    private final ModalDialog dialog;

    private ClickHandler onFinishHandler;
    private PageSwitcher pageSwitcher;

    public WizardDialog(final PageSwitcher pageSwitcher) {
        this.dialog = new ModalDialog();
        this.pageSwitcher = pageSwitcher;

        showPage(pageSwitcher.getNext());

        dialog.getBackButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                try {
                    informPageAboutLeave(true);
                    showPage(pageSwitcher.getPrevious());
                } catch (VetoPageLeaveException e) {
                    // veto exception
                }
            }
        });
        dialog.getPrimaryButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (pageSwitcher.isFinishPage() && pageSwitcher.getCurrentPage().isValid()) { // first check whether finish page and only then switch
                    onFinishHandler.onClick(event);
                }

                informPageAboutLeave(false);
                showPage(pageSwitcher.getNext());

            }
        });
    }

    private void informPageAboutLeave(boolean back) throws VetoPageLeaveException {
        if (getCurrentPage() != null) {
            if (!back && !getCurrentPage().isValid()) {
                throw new VetoPageLeaveException();
            }
            getCurrentPage().beforeLeave();
        }
    }

    public PageSwitcher getPageSwitcher() {
        return pageSwitcher;
    }

    public WizardPage getCurrentPage() {
        return pageSwitcher.getCurrentPage();
    }

    private void showPage(WizardPage page) {
        dialog.getModalBody().clear();
        dialog.getModalBody().add(page.asWidget());

        dialog.getBackButton().setVisible(pageSwitcher.hasPrevious());

        page.onShow(this);
    }

    public WizardDialog setTitle(String title) {
        this.dialog.setDialogTitle(title);
        return this;
    }

    public WizardDialog show() {
        dialog.show();
        return this;
    }

    public WizardDialog hide() {
        dialog.hide();
        return this;
    }

    public ModalDialog getDialog() {
        return dialog;
    }

    public ClickHandler getOnFinishHandler() {
        return onFinishHandler;
    }

    public void setOnFinishHandler(ClickHandler onFinishHandler) {
        this.onFinishHandler = onFinishHandler;
    }
}
