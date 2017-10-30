package org.activityinfo.ui.client.input.view.period;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.i18n.client.LocaleInfo;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.form.StringComboBox;
import org.activityinfo.model.date.Month;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.ui.client.input.viewModel.KeyedSubFormViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.activityinfo.model.resource.ResourceId.generatedPeriodSubmissionId;

public class Monthly implements PeriodSelector {

    private static final List<String> MONTHS = Arrays.asList(LocaleInfo.getCurrentLocale().getDateTimeConstants().months());

    private SimpleEventBus eventBus = new SimpleEventBus();

    private final StringComboBox yearBox;
    private final StringComboBox monthBox;
    private RecordRef parentRecordRef;

    public Monthly(RecordRef parentRecordRef) {
        this.parentRecordRef = parentRecordRef;
        yearBox = new StringComboBox(yearList());
        yearBox.setTriggerAction(ComboBoxCell.TriggerAction.ALL);
        yearBox.addSelectionHandler(this::onSelection);

        monthBox = new StringComboBox(MONTHS);
        monthBox.setForceSelection(true);
        monthBox.setTriggerAction(ComboBoxCell.TriggerAction.ALL);
        monthBox.addSelectionHandler(this::onSelection);
    }

    private void onSelection(SelectionEvent<String> selection) {
        int year = Integer.parseInt(yearBox.getText());
        int monthOfYear = MONTHS.indexOf(monthBox.getText()) + 1;

        Month month = new Month(year, monthOfYear);

        SelectionEvent.fire(this, generatedPeriodSubmissionId(parentRecordRef.getRecordId(), month.toString()));
    }

    private List<String> yearList() {
        List<String> years = new ArrayList<>();
        int year =  Month.of(new Date()).getYear() - 5;
        while(years.size() < 10) {
            years.add(Integer.toString(year++));
        }
        return years;
    }

    @Override
    public List<Component> getToolBarItems() {
        return Arrays.asList(yearBox, monthBox);
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
        String monthKey = subRecordId.substring(subRecordId.length() - 7);
        Month month = Month.parseMonth(monthKey);
        yearBox.setText(Integer.toString(month.getYear()));
        monthBox.setText(MONTHS.get(month.getMonth() - 1));
    }
}
