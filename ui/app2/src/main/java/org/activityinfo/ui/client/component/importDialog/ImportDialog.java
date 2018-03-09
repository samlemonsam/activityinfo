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
package org.activityinfo.ui.client.component.importDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import org.activityinfo.ui.client.widget.ModalTitle;

/**
 * Dialog box that contains the steps of the import process
 */
public class ImportDialog extends ResizeComposite {

    private static ImportDialogUiBinder uiBinder = GWT
            .create(ImportDialogUiBinder.class);

    interface ImportDialogUiBinder extends UiBinder<Widget, ImportDialog> {
    }

    @UiField
    ModalTitle titleWidget;

    @UiField
    Button cancelButton;

    @UiField
    Button nextButton;

    @UiField
    Button prevButton;

    @UiField
    Button finishButton;

    @UiField
    SimpleLayoutPanel pagePanel;

    @UiField
    SpanElement statusText;

    public ImportDialog() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public Button getNextButton() {
        return nextButton;
    }

    public Button getFinishButton() {
        return finishButton;
    }

    public Button getPreviousButton() { return prevButton; }

    public HasClickHandlers getCancelButton() {
        return cancelButton;
    }

    public void setPage(IsWidget page) {
        pagePanel.setWidget(page);
    }

    public void setStatusText(String text) {
        statusText.setInnerHTML(text);
    }

    public SpanElement getStatusText() {
        return statusText;
    }

    public HasText getTitleWidget() {
        return titleWidget;
    }

    public void setCancelButtonVisible(boolean visible) {
      cancelButton.setVisible(visible);
    }
}

