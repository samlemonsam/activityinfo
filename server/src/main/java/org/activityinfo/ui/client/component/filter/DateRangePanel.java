package org.activityinfo.ui.client.component.filter;

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

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.ui.client.component.filter.FilterToolBar.ApplyFilterEvent;
import org.activityinfo.ui.client.component.filter.FilterToolBar.ApplyFilterHandler;
import org.activityinfo.ui.client.component.filter.FilterToolBar.RemoveFilterEvent;
import org.activityinfo.ui.client.component.filter.FilterToolBar.RemoveFilterHandler;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

import java.util.Date;

/**
 * UI Component for selecting a range of dates to be used with a
 * {@link org.activityinfo.legacy.shared.command.Filter}
 *
 * @author Alex Bertram
 */
public class DateRangePanel extends ContentPanel implements HasValue<Filter>, FilterPanel {

    private DateType dateType;

    public enum DateType {
        START,
        END
    }
    
    private DateField minField;
    private DateField maxField;

    private FilterToolBar filterToolBar;

    public DateRangePanel(DateType dateType) {
        super();
        this.dateType = dateType;

        initializeComponent();

        createFilterToolbar();
        createFromDateField();
        createToDateField();
    }

    private void createFilterToolbar() {
        filterToolBar = new FilterToolBar();
        filterToolBar.addApplyFilterHandler(new ApplyFilterHandler() {
            @Override
            public void onApplyFilter(ApplyFilterEvent deleteEvent) {
                applyFilter();
            }
        });

        filterToolBar.addRemoveFilterHandler(new RemoveFilterHandler() {
            @Override
            public void onRemoveFilter(RemoveFilterEvent deleteEvent) {
                removeFilter();
            }
        });
        filterToolBar.setApplyFilterEnabled(true);
        setTopComponent(filterToolBar);
    }

    protected void applyFilter() {
        Filter value = getValue();
        ValueChangeEvent.fire(this, value);
        filterToolBar.setRemoveFilterEnabled(isRestricted(value));
    }

    private boolean isRestricted(Filter value) {
        switch (dateType) {
            case START:
                return value.isStartDateRestricted();
            case END:
                return value.isEndDateRestricted();
            default:
                throw new IllegalStateException("dateType: " + dateType);
        }
    }

    protected void removeFilter() {
        minField.setValue(null);
        maxField.setValue(null);
        filterToolBar.setRemoveFilterEnabled(false);
        ValueChangeEvent.fire(this, getValue());
    }

    private void createToDateField() {
        add(new LabelField(I18N.CONSTANTS.toDate()));

        maxField = new DateField();
        add(maxField);
    }

    private void createFromDateField() {
        add(new LabelField(I18N.CONSTANTS.fromDate()));

        minField = new DateField();
        add(minField);
    }

    private void initializeComponent() {
        switch (dateType) {
            case START:
                setHeadingText(I18N.CONSTANTS.filterByStartDate());
                break;
            case END:
                setHeadingText(I18N.CONSTANTS.filterByEndDate());
                break;
        }
        setIcon(IconImageBundle.ICONS.filter());
    }

    /**
     * Updates the given filter with the user's choice.
     *
     * @param filter the filter to update
     */
    public void updateFilter(Filter filter) {
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Filter> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public Filter getValue() {
        Filter filter = new Filter();
        switch (dateType) {
            case START:
                filter.getStartDateRange().setMinDate(minField.getValue());
                filter.getStartDateRange().setMaxDate(maxField.getValue());
                break;
            case END:
                filter.getEndDateRange().setMinDate(minField.getValue());
                filter.getEndDateRange().setMaxDate(maxField.getValue());
                break;
        }

        return filter;
    }

    @Override
    public void setValue(Filter value) {
        setValue(value, false);
    }

    @Override
    public void setValue(Filter value, boolean fireEvents) {
        switch (dateType) {
            case START:
                minField.setValue(value.getStartDateRange().getMinDate());
                maxField.setValue(value.getStartDateRange().getMaxDate());               
                break;
            case END:
                minField.setValue(value.getEndDateRange().getMinDate());
                maxField.setValue(value.getEndDateRange().getMaxDate());
                break;
        }

        filterToolBar.setRemoveFilterEnabled(isRestricted(value));

        if (fireEvents) {
            ValueChangeEvent.fire(this, getValue());
        }
    }

    public Date getMinDate() {
        return minField.getValue();
    }

    public Date getMaxDate() {
        return maxField.getValue();
    }

    public void setMinDate(Date date) {
        minField.setValue(date);
    }

    public void setMaxDate(Date date) {
        maxField.setValue(date);
    }

    @Override
    public void applyBaseFilter(Filter filter) {

    }
}
