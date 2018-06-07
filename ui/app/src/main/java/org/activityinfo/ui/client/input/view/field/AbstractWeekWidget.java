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
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.model.type.time.Month;
import org.activityinfo.model.type.time.PeriodType;
import org.activityinfo.model.type.time.PeriodValue;
import org.activityinfo.ui.client.input.model.FieldInput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Common Base class for widgets for {@link org.activityinfo.model.type.time.EpiWeekType} and
 * {@link org.activityinfo.model.type.time.FortnightType}
 */
abstract class AbstractWeekWidget<T extends PeriodValue> implements PeriodFieldWidget {

    private static final Css3DateCellAppearance.Css3DateCellResources DATE_RESOURCES =
            GWT.create(Css3DateCellAppearance.Css3DateCellResources.class);

    private final PeriodType periodType;
    private final List<String> periodNames;
    private final FieldUpdater updater;

    private final StringComboBox yearBox;
    private final StringComboBox weekBox;
    private final TextButton pickButton;
    private final DateMenu dateMenu;
    private final CssFloatLayoutContainer panel;

    AbstractWeekWidget(PeriodType periodType, FieldUpdater updater) {
        this.periodType = periodType;
        this.periodNames = periodList();
        this.updater = updater;

        yearBox = new StringComboBox(yearList());
        yearBox.setTriggerAction(ComboBoxCell.TriggerAction.ALL);
        yearBox.addSelectionHandler(this::onSelection);
        yearBox.setWidth(100);

        weekBox = new StringComboBox(periodNames);
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

    @Override
    public void focus() {
        yearBox.focus();
    }

    private void onDatePickerSelected(SelectEvent event) {
        FieldInput input = input();
        if(input.getState() == FieldInput.State.VALID) {
            PeriodValue value = (PeriodValue) input.getValue();
            dateMenu.getDatePicker().setValue(value.asInterval().getStartDate().atMidnightInMyTimezone());
        } else {
            dateMenu.getDatePicker().setValue(new Date());
        }
        dateMenu.show(pickButton);
    }

    private void onDatePicked(ValueChangeEvent<Date> event) {
        LocalDate date = new LocalDate(event.getValue());

        dateMenu.hide();
        updater.update(new FieldInput(periodType.containingDate(date)));
    }

    private List<String> yearList() {
        List<String> years = new ArrayList<>();
        int year =  Month.of(new Date()).getYear() - 5;
        while(years.size() < 10) {
            years.add(Integer.toString(year++));
        }
        return years;
    }

    protected abstract List<String> periodList();

    protected abstract String yearName(T period);

    protected abstract String periodName(T period);

    protected abstract FieldInput parseInput(int year, int periodIndex);

    private FieldInput input() {
        try {


            int year = Integer.parseInt(yearBox.getText());
            int periodIndex = periodNames.indexOf(weekBox.getText());
            if(periodIndex == -1) {
                return FieldInput.INVALID_INPUT;
            }

            return parseInput(year, periodIndex);

        } catch (Exception e) {
            return FieldInput.INVALID_INPUT;
        }
    }

    private void onSelection(SelectionEvent<String> event) {
        updater.update(input());
    }

    @Override
    public final List<Component> asToolBarItems() {
        return Arrays.asList(yearBox, weekBox, pickButton);
    }

    @Override
    public final void init(FieldValue value) {
        if(value != null) {
            yearBox.setText(yearName((T) value));
            weekBox.setText(periodName((T) value));
        }
    }

    @Override
    public final void clear() {
        yearBox.clear();
        weekBox.clear();
    }

    @Override
    public final void setRelevant(boolean relevant) {
        yearBox.setEnabled(relevant);
        weekBox.setEnabled(relevant);
    }

    @Override
    public final Widget asWidget() {
        return panel;
    }
}
