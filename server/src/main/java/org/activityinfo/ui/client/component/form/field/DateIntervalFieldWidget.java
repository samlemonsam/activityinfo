package org.activityinfo.ui.client.component.form.field;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.ui.client.widget.DateBox;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.model.type.time.LocalDateInterval;
import org.activityinfo.promise.Promise;

import java.util.Date;

public class DateIntervalFieldWidget implements FormFieldWidget<LocalDateInterval> {


    interface DateIntervalFieldWidgetUiBinder extends UiBinder<HTMLPanel, DateIntervalFieldWidget> {
    }

    private static DateIntervalFieldWidgetUiBinder ourUiBinder = GWT.create(DateIntervalFieldWidgetUiBinder.class);


    private final HTMLPanel rootElement;

    @UiField
    DateBox startDateBox;
    @UiField
    DateBox endDateBox;

    private ValueUpdater<LocalDateInterval> valueUpdater;

    public DateIntervalFieldWidget(final ValueUpdater<LocalDateInterval> valueUpdater) {
        this.valueUpdater = valueUpdater;
        rootElement = ourUiBinder.createAndBindUi(this);
    }

    @UiHandler("startDateBox")
    public void onStartDateChanged(ValueChangeEvent<Date> event) {
        valueUpdater.update(getValue());
    }


    @UiHandler("endDateBox")
    public void onEndDateChanged(ValueChangeEvent<Date> event) {

    }

    private LocalDateInterval getValue() {
        Date startDate = startDateBox.getValue();
        Date endDate = endDateBox.getValue();

        if(startDate != null && endDate != null &&
           (startDate.equals(endDate) || startDate.before(endDate))) {
            return new LocalDateInterval(new LocalDate(startDate), new LocalDate(endDate));

        } else {
            // TODO: how do we signal the container that the value is invalid?
            return null;
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        startDateBox.setReadOnly(readOnly);
        endDateBox.setReadOnly(readOnly);
    }

    @Override
    public Promise<Void> setValue(LocalDateInterval value) {
        startDateBox.setValue(value.getStartDate().atMidnightInMyTimezone());
        endDateBox.setValue(value.getEndDate().atMidnightInMyTimezone());
        return Promise.done();
    }

    @Override
    public void setType(FieldType type) {

    }

    @Override
    public void clearValue() {
        startDateBox.setValue(null);
        endDateBox.setValue(null);
    }

    @Override
    public Widget asWidget() {
        return rootElement;
    }
}