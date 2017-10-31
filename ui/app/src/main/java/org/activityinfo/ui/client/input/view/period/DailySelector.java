package org.activityinfo.ui.client.input.view.period;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.event.BeforeSelectEvent;
import com.sencha.gxt.widget.core.client.form.DateField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.ui.client.input.viewModel.KeyedSubFormViewModel;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.activityinfo.model.resource.ResourceId.generatedPeriodSubmissionId;

public class DailySelector implements PeriodSelector {

    private SimpleEventBus eventBus = new SimpleEventBus();

    private final DateField dateField;
    private RecordRef parentRecordRef;

    public DailySelector(RecordRef parentRecordRef) {
        this.parentRecordRef = parentRecordRef;
        this.dateField = new DateField();
        this.dateField.addValueChangeHandler(this::onDateChanged);
    }

    private void onDateChanged(ValueChangeEvent<Date> event) {
        LocalDate date = new LocalDate(event.getValue());
        SelectionEvent.fire(this, generatedPeriodSubmissionId(parentRecordRef.getRecordId(), date.toString()));

    }

    @Override
    public List<Component> getToolBarItems() {
        return Collections.singletonList(dateField);
    }

    @Override
    public HandlerRegistration addBeforeSelectHandler(BeforeSelectEvent.BeforeSelectHandler handler) {
        return eventBus.addHandler(BeforeSelectEvent.getType(), handler);
    }

    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<ResourceId> handler) {
        return eventBus.addHandler(SelectionEvent.getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEvent(event);
    }

    public void update(KeyedSubFormViewModel viewModel) {
        RecordRef activeRecordRef = viewModel.getActiveRecordRef();

        // The month in incorporated into the sub record's id:
        // {parentRecordId}-0000-00
        String subRecordId = activeRecordRef.getRecordId().asString();
        String dayKey = subRecordId.substring(subRecordId.length() - 10);
        LocalDate day = LocalDate.parse(dayKey);
        dateField.setValue(day.atMidnightInMyTimezone());
    }

}
