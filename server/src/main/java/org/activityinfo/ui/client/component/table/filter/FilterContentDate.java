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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
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
import org.activityinfo.ui.client.widget.ButtonWithSize;
import org.activityinfo.ui.client.widget.DateRangeDialog;
import org.activityinfo.ui.client.widget.RadioButton;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author yuriyz on 07/03/2015.
 */
public class FilterContentDate extends Composite implements FilterContent {

    interface DateUiBinder extends UiBinder<HTMLPanel, FilterContentDate> {
    }

    private static DateUiBinder uiBinder = GWT.create(DateUiBinder.class);

    @UiField
    SpanElement startDate;
    @UiField
    SpanElement endDate;
    @UiField
    HTMLPanel radioContainer;
    @UiField
    ButtonWithSize customDateRange;

    private final InstanceTable table;
    private final FieldColumn column;
    private final Map<String, LocalDateRange> radioKeyToRange = Maps.newHashMap();

    private LocalDateRange currentRange = null;

    public FilterContentDate(InstanceTable table, FieldColumn column) {
        this.table = table;
        this.column = column;
        initWidget(uiBinder.createAndBindUi(this));

        addLastForQuarters();
        radioContainer.add(new HTML("<hr/>"));
        addYearRange(0);
        addYearRange(1);

        initByCriteriaVisit();
        initCustomDateRange();
    }

    private void initCustomDateRange() {
        customDateRange.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final DateRangeDialog dialog = new DateRangeDialog();
                dialog.setSuccessCallback(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        currentRange = dialog.getDateRange().asLocalDateRange();
                        onRangeChange(currentRange);
                    }
                });
                dialog.show();
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
                        currentRange = fieldCriteria.getRange();
                        onRangeChange(currentRange);
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

    private void addLastForQuarters() {

        List<Map.Entry<Pair<Integer, Integer>, LocalDateRange>> entries = Lists.newArrayList(CalendarUtils.getLastFourQuarterMap().entrySet());
        Collections.reverse(entries);

        for (Map.Entry<Pair<Integer, Integer>, LocalDateRange> entry : entries) {
            Integer year = entry.getKey().getFirst();
            Integer quarter = entry.getKey().getSecond();
            String key = year + "_" + quarter;

            radioContainer.add(createRadioButton(key, I18N.MESSAGES.quarter(year, (quarter + 1))));
            radioKeyToRange.put(key, entry.getValue());
        }
    }

    private RadioButton createRadioButton(String key, String label) {
        final RadioButton button = new RadioButton("range_name", label);
        button.setFormValue(key);
        button.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (button.getValue()) {
                    onRangeChange(radioKeyToRange.get(button.getFormValue()));
                }
            }
        });
        return button;
    }

    private void onRangeChange(LocalDateRange range) {
        if (range == null) {
            startDate.setInnerHTML("");
            endDate.setInnerHTML("");
            return;
        }

        startDate.setInnerText(format(range.getMinLocalDate()));
        endDate.setInnerText(format(range.getMaxLocalDate()));

    }

    private static String format(LocalDate localDate) {
        return localDate != null ? localDate.toString() : "";
    }

    private void addYearRange(int yearsAgo) {
        int year = new LocalDate().getYear() - yearsAgo;
        LocalDate from = new LocalDate(year, 0, 1);
        LocalDate to = new LocalDate(year, 11, 31);

        radioContainer.add(createRadioButton(year + "", year + ""));
        radioKeyToRange.put(year + "", new LocalDateRange(from, to));
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
        currentRange = null;
        onRangeChange(null);
    }

    @Override
    public Widget asWidget() {
        return this;
    }
}
