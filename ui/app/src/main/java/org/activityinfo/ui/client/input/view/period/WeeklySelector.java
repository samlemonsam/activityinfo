package org.activityinfo.ui.client.input.view.period;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.event.BeforeSelectEvent;
import com.sencha.gxt.widget.core.client.form.StringComboBox;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.time.EpiWeek;
import org.activityinfo.model.type.time.Month;
import org.activityinfo.ui.client.input.viewModel.KeyedSubFormViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.activityinfo.model.resource.ResourceId.generatedPeriodSubmissionId;

public class WeeklySelector implements PeriodSelector {

    private SimpleEventBus eventBus = new SimpleEventBus();

    private final StringComboBox yearBox;
    private final StringComboBox weekBox;
    private RecordRef parentRecordRef;

    public WeeklySelector(RecordRef parentRecordRef) {
        this.parentRecordRef = parentRecordRef;
        yearBox = new StringComboBox(yearList());
        yearBox.setTriggerAction(ComboBoxCell.TriggerAction.ALL);
        yearBox.addSelectionHandler(this::onSelection);

        weekBox = new StringComboBox(weekList());
        weekBox.setForceSelection(true);
        weekBox.setTriggerAction(ComboBoxCell.TriggerAction.ALL);
        weekBox.addSelectionHandler(this::onSelection);
    }

    private List<String> yearList() {
        List<String> years = new ArrayList<>();
        int year =  Month.of(new Date()).getYear() - 5;
        while(years.size() < 10) {
            years.add(Integer.toString(year++));
        }
        return years;
    }

    private List<String> weekList() {
        List<String> weeks = new ArrayList<>();
        for (int i = 0; i < EpiWeek.WEEKS_IN_YEAR; i++) {
            weeks.add(weekName(i));
        }
        return weeks;
    }

    private String weekName(int i) {
        return "W" + i;
    }

    private void onSelection(SelectionEvent<String> selection) {
        int year = Integer.parseInt(yearBox.getText());
        int weekNumber = Integer.parseInt(weekBox.getText().substring(1));

        EpiWeek week = new EpiWeek(weekNumber, year);

        SelectionEvent.fire(this, generatedPeriodSubmissionId(parentRecordRef.getRecordId(), week.toString()));
    }

    @Override
    public List<Component> getToolBarItems() {
        return Arrays.asList(yearBox, weekBox);
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
        String weekKey = subRecordId.substring(subRecordId.length() - 7);
        EpiWeek week = EpiWeek.parse(weekKey);
        yearBox.setText(Integer.toString(week.getYear()));
        weekBox.setText(weekName(week.getWeekInYear()));
    }
}
