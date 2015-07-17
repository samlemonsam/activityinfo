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
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.core.shared.type.formatter.DateFormatterFactory;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.date.CalendarUtils;
import org.activityinfo.model.date.DateRange;

import java.util.Date;

/**
 * @author yuriyz on 07/03/2015.
 */
public class DateRangePanel implements IsWidget {

    interface MyUiBinder extends UiBinder<HTMLPanel, DateRangePanel> {
    }

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    private final HTMLPanel rootPanel;

    @UiField(provided = true)
    DateBox fromDate;
    @UiField(provided = true)
    DateBox toDate;
    @UiField
    SpanElement messageSpan;
    @UiField
    HTMLPanel messageSpanContainer;

    public DateRangePanel() {
        fromDate = new DateBox(createFormat());
        toDate = new DateBox(createFormat());

        rootPanel = uiBinder.createAndBindUi(this);
    }


    public DateRangePanel inlineForm(boolean inlineForm) {
        if (inlineForm) {
            rootPanel.addStyleName("form-inline");
        } else {
            rootPanel.removeStyleName("form-inline");
        }
        return this;
    }


    public void addValueChangeHandler(ValueChangeHandler<Date> rangeChangedHandler) {
        fromDate.addValueChangeHandler(rangeChangedHandler);
        toDate.addValueChangeHandler(rangeChangedHandler);
    }

    public boolean validate() {
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

    @Override
    public Widget asWidget() {
        return rootPanel;
    }

    public DateRangePanel clear() {
        fromDate.setValue(null);
        toDate.setValue(null);
        return this;
    }

    public DateRange getDateRange() {
        return new DateRange(fromDate.getValue(), toDate.getValue());
    }

    public DateRangePanel setDateRange(DateRange dateRange) {
        if (dateRange == null) {
            clear();
            return this;
        }
        fromDate.setValue(dateRange.getStart());
        toDate.setValue(dateRange.getEnd());
        return this;
    }

    public static DateBox.Format createFormat() {
        return new DateBox.DefaultFormat(DateTimeFormat.getFormat(DateFormatterFactory.FORMAT));
    }


}
