package org.activityinfo.ui.client.input.view.field;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.theme.triton.client.base.field.Css3DateCellAppearance;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.CssFloatLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.StringComboBox;
import com.sencha.gxt.widget.core.client.menu.DateMenu;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.time.EpiWeek;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.model.type.time.Month;
import org.activityinfo.ui.client.input.model.FieldInput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class WeekWidget implements PeriodFieldWidget {

    private static final Css3DateCellAppearance.Css3DateCellResources DATE_RESOURCES =
            GWT.create(Css3DateCellAppearance.Css3DateCellResources.class);

    private final StringComboBox yearBox;
    private final StringComboBox weekBox;
    private final TextButton pickButton;
    private final DateMenu dateMenu;
    private final CssFloatLayoutContainer panel;

    private final FieldUpdater updater;

    public WeekWidget(FieldUpdater updater) {
        this.updater = updater;

        yearBox = new StringComboBox(yearList());
        yearBox.setTriggerAction(ComboBoxCell.TriggerAction.ALL);
        yearBox.addSelectionHandler(this::onSelection);
        yearBox.setWidth(100);

        weekBox = new StringComboBox(weekList());
        weekBox.setForceSelection(true);
        weekBox.setTriggerAction(ComboBoxCell.TriggerAction.ALL);
        weekBox.addSelectionHandler(this::onSelection);

        dateMenu = new DateMenu();
        dateMenu.getDatePicker().addValueChangeHandler(this::onDatePicked);

        pickButton = new TextButton();
        pickButton.setIcon(DATE_RESOURCES.triggerArrow());
        pickButton.addSelectHandler(this::onDatePickerSelected);

        panel = new CssFloatLayoutContainer();
        panel.add(yearBox, new CssFloatLayoutContainer.CssFloatData(0.5));
        panel.add(weekBox, new CssFloatLayoutContainer.CssFloatData(0.5));
    }


    private void onDatePickerSelected(SelectEvent event) {
        FieldInput input = input();
        if(input.getState() == FieldInput.State.VALID) {
            EpiWeek value = (EpiWeek) input.getValue();
            dateMenu.getDatePicker().setValue(value.asInterval().getStartDate().atMidnightInMyTimezone());
        } else {
            dateMenu.getDatePicker().setValue(new Date());
        }
        dateMenu.show(pickButton);
    }

    private void onDatePicked(ValueChangeEvent<Date> event) {
        LocalDate date = new LocalDate(event.getValue());
        EpiWeek week = EpiWeek.weekOf(date);
        dateMenu.hide();
        updater.update(new FieldInput(week));
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

    private FieldInput input() {
        try {
            int year = Integer.parseInt(yearBox.getText());
            int weekNumber = Integer.parseInt(weekBox.getText().substring(1));

            return new FieldInput(new EpiWeek(year, weekNumber));

        } catch (Exception e) {
            return FieldInput.INVALID_INPUT;
        }
    }


    private void onSelection(SelectionEvent<String> event) {
        updater.update(input());
    }

    @Override
    public List<Component> asToolBarItems() {
        return Arrays.asList(yearBox, weekBox, pickButton);
    }

    @Override
    public void init(FieldValue value) {
        if(value instanceof EpiWeek) {
            EpiWeek week = (EpiWeek) value;
            yearBox.setText(Integer.toString(week.getYear()));
            weekBox.setText(weekName(week.getWeekInYear()));
        }
    }

    @Override
    public void clear() {
        yearBox.clear();
        weekBox.clear();
    }

    @Override
    public void setRelevant(boolean relevant) {
        yearBox.setEnabled(relevant);
        weekBox.setEnabled(relevant);
    }

    @Override
    public Widget asWidget() {
        return panel;
    }
}
