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

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.container.CssFloatLayoutContainer;
import com.sencha.gxt.widget.core.client.form.StringComboBox;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.time.Month;
import org.activityinfo.ui.client.input.model.FieldInput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MonthWidget implements PeriodFieldWidget {

    private static final List<String> MONTHS = Arrays.asList(LocaleInfo.getCurrentLocale().getDateTimeConstants().months());

    private final StringComboBox yearBox;
    private final StringComboBox monthBox;
    private FieldUpdater updater;
    private final CssFloatLayoutContainer panel;

    public MonthWidget(FieldUpdater updater) {
        this.updater = updater;
        yearBox = new StringComboBox(yearList());
        yearBox.setTriggerAction(ComboBoxCell.TriggerAction.ALL);
        yearBox.addSelectionHandler(this::onSelection);

        monthBox = new StringComboBox(MONTHS);
        monthBox.setForceSelection(true);
        monthBox.setTriggerAction(ComboBoxCell.TriggerAction.ALL);
        monthBox.addSelectionHandler(this::onSelection);

        panel = new CssFloatLayoutContainer();
        panel.add(yearBox, new CssFloatLayoutContainer.CssFloatData(0.5));
        panel.add(monthBox, new CssFloatLayoutContainer.CssFloatData(0.5));
    }

    private List<String> yearList() {
        List<String> years = new ArrayList<>();
        int year =  Month.of(new Date()).getYear() - 5;
        while(years.size() < 10) {
            years.add(Integer.toString(year++));
        }
        return years;
    }

    private FieldInput input() {
        try {
            int year = Integer.parseInt(yearBox.getText());
            int monthOfYear = MONTHS.indexOf(monthBox.getText()) + 1;
            return new FieldInput(new Month(year, monthOfYear));

        } catch (Exception e) {
            return FieldInput.INVALID_INPUT;
        }
    }

    private void onSelection(SelectionEvent<String> selection) {
        updater.update(input());
    }

    @Override
    public void init(FieldValue value) {
        if (value instanceof Month) {
            Month month = (Month) value;
            yearBox.setText(Integer.toString(month.getYear()));
            monthBox.setText(MONTHS.get(month.getMonth() - 1));
        }
    }

    @Override
    public void clear() {
        yearBox.clear();
        monthBox.clear();
    }

    @Override
    public void setRelevant(boolean relevant) {
        yearBox.setEnabled(false);
        monthBox.setEnabled(false);
    }

    @Override
    public void focus() {
        yearBox.focus();
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

    @Override
    public List<Component> asToolBarItems() {
        return Arrays.asList(yearBox, monthBox);
    }
}
