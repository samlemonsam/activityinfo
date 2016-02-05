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
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.date.CalendarUtils;
import org.activityinfo.model.date.DateRange;
import org.activityinfo.model.date.EpiWeek;
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
public class PeriodInstanceKeyedGenerator {

    private static final int MAXIMUM_SIZE = 100000;

    public static final ResourceId PERIOD_START_DATE_ID = ResourceId.valueOf("_period_start_date");
    public static final ResourceId PERIOD_END_DATE_ID = ResourceId.valueOf("_period_end_date");

    public enum Direction {
        BACK, FORWARD
    }

    public interface Formatter {
        String format(String pattern, Date date);
    }

    private final ResourceId classId;
    private final Formatter formatter;
    private final CalendarUtils.DayOfWeekProvider dayOfWeekProvider;

    private List<FormInstance> lastGeneratedList;
    private PeriodValue lastPeriod;
    private int lastCount = 1;


    public PeriodInstanceKeyedGenerator(ResourceId classId) {
        this(classId, new Formatter() {
            @Override
            public String format(String pattern, Date date) {
                return DateTimeFormat.getFormat(pattern).format(date);
            }
        });
    }

    public PeriodInstanceKeyedGenerator(ResourceId classId, Formatter formatter) {
        this(classId, formatter, CalendarUtils.GWT_DAY_OF_WEEK_PROVIDER);
    }

    public PeriodInstanceKeyedGenerator(ResourceId classId, Formatter formatter, CalendarUtils.DayOfWeekProvider dayOfWeekProvider) {
        this.classId = classId;
        this.formatter = formatter;
        this.dayOfWeekProvider = dayOfWeekProvider;
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
            pointToCalculate = dateRange.midDate();
            result.add(createInstance(dateRange, period, direction));
        }

        if (direction == Direction.BACK) {
            Collections.reverse(result);
        }
        if (saveState) {
            lastGeneratedList = result;
            lastPeriod = period;
            lastCount = count;
        }
        return result;
    }

    public List<FormInstance> next() {
        assertState();
        int size = lastGeneratedList.size();
        FormInstance lastInstance = lastGeneratedList.get(size - 1);
        DateRange lastDateRange = getDateRangeFromInstance(lastInstance);
        Date point = PredefinedPeriods.DAILY.getPeriod().equals(lastPeriod) ? lastDateRange.getEnd() : lastDateRange.midDate();
        List<FormInstance> next = generate(lastPeriod, point, Direction.FORWARD, 1, false);
        lastGeneratedList.remove(0); // remove first
        lastGeneratedList.add(size - 1, next.get(0)); // add next at the end
        return lastGeneratedList;
    }

    public List<FormInstance> fullNext() {
        assertState();
        int size = lastGeneratedList.size();
        FormInstance lastInstance = lastGeneratedList.get(size - 1);
        DateRange lastDateRange = getDateRangeFromInstance(lastInstance);
        return generate(lastPeriod, lastDateRange.midDate(), Direction.FORWARD, lastCount, true);
    }

    public List<FormInstance> previous() {
        assertState();
        int size = lastGeneratedList.size();
        FormInstance firstInstance = lastGeneratedList.get(0);
        DateRange firstDateRange = getDateRangeFromInstance(firstInstance);
        List<FormInstance> previous = generate(lastPeriod, firstDateRange.midDate(), Direction.BACK, 1, false);
        lastGeneratedList.remove(size - 1); // remove last
        lastGeneratedList.add(0, previous.get(0)); // add next at the beginning
        return lastGeneratedList;
    }

    public List<FormInstance> fullPrevious() {
        assertState();
        FormInstance firstInstance = lastGeneratedList.get(0);
        DateRange firstDateRange = getDateRangeFromInstance(firstInstance);
        return generate(lastPeriod, firstDateRange.midDate(), Direction.BACK, lastCount, true);
    }

    private void assertState() {
        Preconditions.checkState(lastGeneratedList != null && !lastGeneratedList.isEmpty(),
                "There are no generated instances. Please generate instances first");
        Preconditions.checkNotNull(lastPeriod);
    }

    public static DateRange getDateRangeFromInstance(FormInstance instance) {
        LocalDate startDate = instance.getDate(PERIOD_START_DATE_ID);
        LocalDate endDate = instance.getDate(PERIOD_END_DATE_ID);
        return new DateRange(startDate.atMidnightInMyTimezone(), endDate.atMidnightInMyTimezone());
    }

    private FormInstance createInstance(DateRange range, PeriodValue period, Direction direction) {
        FormInstance instance = KeyInstanceGenerator.newKeyedInstance(range, classId);
        instance.set(PERIOD_START_DATE_ID, range.getStart());
        instance.set(PERIOD_END_DATE_ID, range.getEnd());
        FormInstanceLabeler.setLabel(instance, getLabel(range, period, direction));
        return instance;
    }

    private String getLabel(DateRange range, PeriodValue period, Direction direction) {
        if (period.equals(PredefinedPeriods.WEEKLY.getPeriod())) {
            EpiWeek epiWeek = CalendarUtils.epiWeek(range.midDate(), dayOfWeekProvider);
            return I18N.CONSTANTS.week() + " " + epiWeek.getWeekInYear() + " " + epiWeek.getYear();
        }
        return format(getDateForLabel(range, period), period);
    }

    private Date getDateForLabel(DateRange range, PeriodValue period) {
        if (period.equals(PredefinedPeriods.MONTHLY.getPeriod())) {
            return range.getStart();
        } else if (period.equals(PredefinedPeriods.YEARLY.getPeriod())) {
            return range.getStart();
        }
        return range.midDate();
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

    private DateRange generateDateRange(PeriodValue period, Date point, Direction direction) {
        // make sure start date is reset, instanceId depends on start/end date of range.
        CalendarUtil.resetTime(point);
        Date copy = CalendarUtil.copyDate(point);
        if (PredefinedPeriods.YEARLY.getPeriod().equals(period)) {
            CalendarUtil.addMonthsToDate(copy, direction == Direction.BACK ? -12 : 12);

            Date startDate = CalendarUtil.copyDate(copy);
            startDate.setMonth(0); // january
            startDate.setDate(1);

            Date endDate = CalendarUtil.copyDate(startDate);
            CalendarUtil.addMonthsToDate(endDate, 12);
            CalendarUtil.addDaysToDate(endDate, -1);

            return new DateRange(startDate, endDate);

        } else if (PredefinedPeriods.MONTHLY.getPeriod().equals(period)) {
            CalendarUtil.addMonthsToDate(copy, direction == Direction.BACK ? -1 : 1);

            Date startDate = CalendarUtil.copyDate(copy);
            CalendarUtil.setToFirstDayOfMonth(startDate);

            Date endDate = CalendarUtil.copyDate(startDate);
            CalendarUtil.addMonthsToDate(endDate, 1);
            CalendarUtil.addDaysToDate(endDate, -1);
            return new DateRange(startDate, endDate);
        } else if (PredefinedPeriods.WEEKLY.getPeriod().equals(period)) {
            CalendarUtil.addDaysToDate(point, direction == Direction.BACK ? -7 : 7);
            return CalendarUtils.rangeByEpiWeekFromDate(dayOfWeekProvider, point);
        } else if (PredefinedPeriods.BI_WEEKLY.getPeriod().equals(period)) {
            CalendarUtil.addDaysToDate(copy, direction == Direction.BACK ? -14 : 14);
        } else if (PredefinedPeriods.DAILY.getPeriod().equals(period)) {
            CalendarUtil.addDaysToDate(copy, direction == Direction.BACK ? -1 : 1);
        } else {
            throw new UnsupportedOperationException("Period is not supported yet, period: " + period);
        }

        if (direction == Direction.BACK) {
            return new DateRange(copy, point);
        } else {
            return new DateRange(point, copy);
        }
    }
}
