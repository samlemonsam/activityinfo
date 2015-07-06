package org.activityinfo.ui.client.widget;
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.date.CalendarUtils;
import org.activityinfo.model.date.DateRange;

import java.util.Date;

/**
 * @author yuriyz on 07/03/2015.
 */
public class DateRangeDialog {

    interface MyUiBinder extends UiBinder<HTMLPanel, DateRangeDialog> {
    }

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    private final ModalDialog dialog = new ModalDialog();
    private ClickHandler successCallback;

    @UiField
    DateBox fromDate;
    @UiField
    DateBox toDate;
    @UiField
    SpanElement messageSpan;
    @UiField
    HTMLPanel messageSpanContainer;

    public DateRangeDialog() {
        dialog.setDialogTitle(I18N.CONSTANTS.customDateRange());
        dialog.getModalBody().add(uiBinder.createAndBindUi(this));

        dialog.getPrimaryButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onOk();
            }
        });
    }

    private void onOk() {
        if (validate()) {
            dialog.hide();
            if (successCallback != null) {
                successCallback.onClick(null);
            }
        }
    }

    private boolean validate() {
        messageSpanContainer.setVisible(false);

        Date fromDateValue = fromDate.getValue();
        Date toDateValue = toDate.getValue();

        if (fromDateValue == null) {
            showError(I18N.CONSTANTS.pleaseSpecifyFromDate());
            return false;
        }
        if (toDateValue == null) {
            showError(I18N.CONSTANTS.pleaseSpecifyToDate());
            return false;
        }
        if (CalendarUtils.before(fromDateValue, toDateValue)) {
            showError(I18N.CONSTANTS.inconsistentDateRangeWarning());
            return false;
        }
        return true;
    }

    private void showError(String message) {
        messageSpanContainer.setVisible(true);
        messageSpan.setInnerHTML(SafeHtmlUtils.fromString(message).asString());
    }

    public DateRangeDialog show() {
        dialog.show();
        return this;
    }

    public DateRange getDateRange() {
        return new DateRange(fromDate.getValue(), toDate.getValue());
    }

    public ClickHandler getSuccessCallback() {
        return successCallback;
    }

    public void setSuccessCallback(ClickHandler successCallback) {
        this.successCallback = successCallback;
    }
}
