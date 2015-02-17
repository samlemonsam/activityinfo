package org.activityinfo.ui.client.component.form.subform;
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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import org.activityinfo.model.date.DateRange;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.FormInstanceLabeler;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.period.PeriodValue;
import org.activityinfo.model.type.period.PredefinedPeriods;
import org.activityinfo.model.type.time.LocalDate;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author yuriyz on 02/05/2015.
 */
public class InstanceGenerator {

    private static final int MAXIMUM_SIZE = 100000;

    public static final ResourceId PERIOD_START_DATE_ID = ResourceId.valueOf("_period_start_date");
    public static final ResourceId PERIOD_END_DATE_ID = ResourceId.valueOf("_period_end_date");

    public static enum Direction {
        BACK, FORWARD
    }

    public static interface Formatter {
        String format(String pattern, Date date);
    }

    private final ResourceId classId;
    private final Formatter formatter;

    private List<FormInstance> lastGeneratedList;
    private PeriodValue lastPeriod;
    private int lastCount = 1;
    private Direction lastDirection;

    public InstanceGenerator(ResourceId classId) {
        this(classId, new Formatter() {
            @Override
            public String format(String pattern, Date date) {
                return DateTimeFormat.getFormat(pattern).format(date);
            }
        });
    }

    public InstanceGenerator(ResourceId classId, Formatter formatter) {
        this.classId = classId;
        this.formatter = formatter;
    }

    public List<FormInstance> generate(PeriodValue period, Date startDate, Direction direction, int count) {
        return generate(period, startDate, direction, count, true);
    }

    private List<FormInstance> generate(PeriodValue period, Date startDate, Direction direction, int count, boolean saveState) {
        Preconditions.checkNotNull(period);
        Preconditions.checkNotNull(startDate);
        Preconditions.checkNotNull(direction);

        Preconditions.checkState(!period.isZero(), "It's not allowed to generate instance with zero period.");
        Preconditions.checkState(count > 0 && count <= MAXIMUM_SIZE, "Count must be more than 0 but less then " + MAXIMUM_SIZE);

        CalendarUtil.resetTime(startDate);
        List<FormInstance> result = Lists.newArrayListWithCapacity(count);
        Date pointToCalculate = startDate;

        for (int i = 0; i < count; i++) {
            DateRange dateRange = generateDateRange(period, pointToCalculate, direction);
            pointToCalculate = getPointToCalculate(dateRange, direction);
            result.add(createInstance(dateRange, period, direction));
        }

        if (direction == Direction.BACK) {
            Collections.reverse(result);
        }
        if (saveState) {
            lastGeneratedList = result;
            lastPeriod = period;
            lastCount = count;
            lastDirection = direction;
        }
        return result;
    }

    public List<FormInstance> next() {
        assertState();
        int size = lastGeneratedList.size();
        FormInstance lastInstance = lastGeneratedList.get(size - 1);
        DateRange lastDateRange = getDateRangeFromInstance(lastInstance);
        Date point = getPointToCalculate(lastDateRange, lastDirection);
        List<FormInstance> next = generate(lastPeriod, point, Direction.FORWARD, 1, false);
        lastGeneratedList.remove(0); // remove first
        lastGeneratedList.add(size - 1, next.get(0)); // add next at the end
        lastDirection = Direction.FORWARD;
        return lastGeneratedList;
    }

    public List<FormInstance> fullNext() {
        assertState();
        int size = lastGeneratedList.size();
        FormInstance lastInstance = lastGeneratedList.get(size - 1);
        DateRange lastDateRange = getDateRangeFromInstance(lastInstance);
        return generate(lastPeriod, getPointToCalculate(lastDateRange, lastDirection), Direction.FORWARD, lastCount, true);
    }

    public List<FormInstance> previous() {
        assertState();
        lastDirection = Direction.BACK;
        int size = lastGeneratedList.size();
        FormInstance firstInstance = lastGeneratedList.get(0);
        DateRange firstDateRange = getDateRangeFromInstance(firstInstance);
        List<FormInstance> previous = generate(lastPeriod, getPointToCalculate(firstDateRange, lastDirection), Direction.BACK, 1, false);
        lastGeneratedList.remove(size - 1); // remove last
        lastGeneratedList.add(0, previous.get(0)); // add next at the beginning
        return lastGeneratedList;
    }

    public List<FormInstance> fullPrevious() {
        assertState();
        FormInstance firstInstance = lastGeneratedList.get(0);
        DateRange firstDateRange = getDateRangeFromInstance(firstInstance);
        return generate(lastPeriod, getPointToCalculate(firstDateRange, lastDirection), Direction.BACK, lastCount, true);
    }

    private void assertState() {
        Preconditions.checkState(lastGeneratedList != null && !lastGeneratedList.isEmpty(),
                "There are no generated instances. Please generate instances first");
        Preconditions.checkNotNull(lastPeriod);
    }

    private static Date getPointToCalculate(DateRange range, Direction direction) {
        return direction == Direction.BACK ? range.getStart() : range.getEnd();
    }

    private static DateRange getDateRangeFromInstance(FormInstance instance) {
        LocalDate startDate = instance.getDate(PERIOD_START_DATE_ID);
        LocalDate endDate = instance.getDate(PERIOD_END_DATE_ID);
        return new DateRange(startDate.atMidnightInMyTimezone(), endDate.atMidnightInMyTimezone());
    }

    private FormInstance createInstance(DateRange range, PeriodValue period, Direction direction) {
        FormInstance instance = new FormInstance(ResourceId.generateId(), classId);
        instance.set(PERIOD_START_DATE_ID, range.getStart());
        instance.set(PERIOD_END_DATE_ID, range.getEnd());
        FormInstanceLabeler.setLabel(instance, format(getPointToCalculate(range, direction), period));
        return instance;
    }

    private String format(Date date, PeriodValue period) {
        if (PredefinedPeriods.YEARLY.getPeriod().equals(period)) {
            return formatter.format("yyyy", date);
        } else if (PredefinedPeriods.MONTHLY.getPeriod().equals(period)) {
            return formatter.format("MMM yyyy", date);
        } else if (PredefinedPeriods.DAILY.getPeriod().equals(period)) {
            return formatter.format("dd MMM yyyy", date);
        }

        throw new UnsupportedOperationException("Period is not supported yet, period: " + period);
        //return DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT).format(date);
    }

    private DateRange generateDateRange(PeriodValue period, Date startDate, Direction direction) {
        Date result = CalendarUtil.copyDate(startDate);
        if (PredefinedPeriods.YEARLY.getPeriod().equals(period)) {
            CalendarUtil.addMonthsToDate(result, direction == Direction.BACK ? -12 : 12);
        } else if (PredefinedPeriods.MONTHLY.getPeriod().equals(period)) {
            CalendarUtil.addMonthsToDate(result, direction == Direction.BACK ? -1 : 1);
        } else if (PredefinedPeriods.WEEKLY.getPeriod().equals(period)) {
            CalendarUtil.addDaysToDate(result, direction == Direction.BACK ? -7 : 7);
        } else if (PredefinedPeriods.BI_WEEKLY.getPeriod().equals(period)) {
            CalendarUtil.addDaysToDate(result, direction == Direction.BACK ? -14 : 14);
        } else if (PredefinedPeriods.DAILY.getPeriod().equals(period)) {
            CalendarUtil.addDaysToDate(result, direction == Direction.BACK ? -1 : 1);
        } else {
            throw new UnsupportedOperationException("Period is not supported yet, period: " + period);
        }

        if (direction == Direction.BACK) {
            return new DateRange(result, startDate);
        } else {
            return new DateRange(startDate, result);
        }
    }
}
