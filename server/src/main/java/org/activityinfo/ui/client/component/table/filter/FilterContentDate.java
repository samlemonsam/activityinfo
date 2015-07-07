package org.activityinfo.ui.client.component.table.filter;
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

import com.bedatadriven.rebar.time.calendar.LocalDate;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.core.shared.criteria.Criteria;
import org.activityinfo.core.shared.criteria.CriteriaUnion;
import org.activityinfo.core.shared.criteria.CriteriaVisitor;
import org.activityinfo.core.shared.criteria.FieldDateCriteria;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.date.CalendarUtils;
import org.activityinfo.model.date.LocalDateRange;
import org.activityinfo.model.util.Pair;
import org.activityinfo.ui.client.component.table.FieldColumn;
import org.activityinfo.ui.client.component.table.InstanceTable;
import org.activityinfo.ui.client.style.ElementStyle;
import org.activityinfo.ui.client.widget.ButtonWithSize;
import org.activityinfo.ui.client.widget.DateRangePanel;

import java.util.Date;
import java.util.Map;

/**
 * @author yuriyz on 07/03/2015.
 */
public class FilterContentDate extends Composite implements FilterContent {

    interface DateUiBinder extends UiBinder<HTMLPanel, FilterContentDate> {
    }

    private static DateUiBinder uiBinder = GWT.create(DateUiBinder.class);

    @UiField
    HTMLPanel buttonsContainer;
    @UiField
    DateRangePanel rangePanel;

    private final InstanceTable table;
    private final FieldColumn column;

    private LocalDateRange currentRange = null;

    public FilterContentDate(InstanceTable table, FieldColumn column) {
        this.table = table;
        this.column = column;
        initWidget(uiBinder.createAndBindUi(this));

        addLastFourQuarters();
        buttonsContainer.add(new HTML("<hr/>"));
        addYearRange(0);
        addYearRange(1);

        initByCriteriaVisit();
        rangePanel.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                setCurrentRange(rangePanel.getDateRange().asLocalDateRange());
            }
        });
    }

    private void initByCriteriaVisit() {
        final Criteria criteria = column.getCriteria();
        if (criteria != null) {
            final CriteriaVisitor initializationVisitor = new CriteriaVisitor() {
                @Override
                public void visitFieldCriteria(FieldDateCriteria fieldCriteria) {
                    if (fieldCriteria.getFieldPath().equals(column.getNode().getPath())) {
                        setCurrentRange(fieldCriteria.getRange());
                        rangePanel.setDateRange(currentRange.asDateRange());
                    }
                }

                @Override
                public void visitUnion(CriteriaUnion criteriaUnion) {
                    for (Criteria criteria : criteriaUnion.getElements()) {
                        criteria.accept(this);
                    }
                }
            };
            criteria.accept(initializationVisitor);
        }
    }

    private void addLastFourQuarters() {
        for (Map.Entry<Pair<Integer, Integer>, LocalDateRange> entry : CalendarUtils.getLastFourQuarterMap().entrySet()) {
            Integer year = entry.getKey().getFirst();
            Integer quarter = entry.getKey().getSecond();

            buttonsContainer.add(createButton(I18N.MESSAGES.quarter(year, (quarter + 1)), entry.getValue()));
        }
    }

    private ButtonWithSize createButton(final String label, final LocalDateRange range) {
        final ButtonWithSize button = new ButtonWithSize(ElementStyle.DEFAULT, ButtonWithSize.Size.EXTRA_SMALL);
        button.setText(label);
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                setCurrentRange(range);
                rangePanel.setDateRange(range.asDateRange());
            }
        });

        return button;
    }

    private void addYearRange(int yearsAgo) {
        int year = new LocalDate().getYear() - yearsAgo;
        LocalDate from = new LocalDate(year, 1, 1);
        LocalDate to = new LocalDate(year, 12, 31);

        buttonsContainer.add(createButton(year + "", new LocalDateRange(from, to)));
    }

    @Override
    public Criteria getCriteria() {
        if (currentRange != null) {
            return new FieldDateCriteria(column.getNode().getPath(), currentRange);
        }
        return null;
    }

    @Override
    public void clear() {
        setCurrentRange(null);
        rangePanel.clear();
    }

    public void setCurrentRange(LocalDateRange currentRange) {
        this.currentRange = currentRange;
        rangePanel.setDateRange(currentRange != null ? currentRange.asDateRange() : null);
    }

    @Override
    public Widget asWidget() {
        return this;
    }
}
