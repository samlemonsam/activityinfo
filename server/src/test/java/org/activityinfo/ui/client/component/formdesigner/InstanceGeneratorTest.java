package org.activityinfo.ui.client.component.formdesigner;
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

import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.date.CalendarUtils;
import org.activityinfo.model.date.DateRange;
import org.activityinfo.model.date.DayOfWeek;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.FormInstanceLabeler;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.period.PredefinedPeriods;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.ui.client.component.form.subform.PeriodInstanceKeyedGenerator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author yuriyz on 02/05/2015.
 */
public class InstanceGeneratorTest {

    private ResourceId parentId = ResourceId.generateId();

    @BeforeClass
    public static void beforeClass() {
        LocaleProxy.initialize();
    }

    @Test
    public void monthlyStateBugWithNextNextBack() {
        PeriodInstanceKeyedGenerator generator = jvmGenerator();

        List<FormInstance> instances = generator.generate(PredefinedPeriods.MONTHLY.getPeriod(), fixedDate(Calendar.FEBRUARY), PeriodInstanceKeyedGenerator.Direction.BACK, 4);

        print(instances, "Monthly BACK 4");

        Assert.assertEquals(instances.size(), 4);
        assertLabel(instances.get(0), "Oct 2014");
        assertLabel(instances.get(1), "Nov 2014");
        assertLabel(instances.get(2), "Dec 2014");
        assertLabel(instances.get(3), "Jan 2015");

        instances = generator.next();

        print(instances, "Monthly NEXT 1");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "Nov 2014");
        assertLabel(instances.get(1), "Dec 2014");
        assertLabel(instances.get(2), "Jan 2015");
        assertLabel(instances.get(3), "Feb 2015");

        instances = generator.next();

        print(instances, "Monthly NEXT 1");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "Dec 2014");
        assertLabel(instances.get(1), "Jan 2015");
        assertLabel(instances.get(2), "Feb 2015");
        assertLabel(instances.get(3), "Mar 2015");

        instances = generator.previous();

        print(instances, "Monthly BACK 1");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "Nov 2014");
        assertLabel(instances.get(1), "Dec 2014");
        assertLabel(instances.get(2), "Jan 2015");
        assertLabel(instances.get(3), "Feb 2015");

        instances = generator.next();

        print(instances, "Monthly NEXT 1");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "Dec 2014");
        assertLabel(instances.get(1), "Jan 2015");
        assertLabel(instances.get(2), "Feb 2015");
        assertLabel(instances.get(3), "Mar 2015");

    }

    @Test
    public void monthlyFirstBack() {

        PeriodInstanceKeyedGenerator generator = jvmGenerator();
        List<FormInstance> instances = generator.generate(PredefinedPeriods.MONTHLY.getPeriod(), fixedDate(Calendar.JANUARY), PeriodInstanceKeyedGenerator.Direction.BACK, 4);

        print(instances, "Monthly BACK 4");

        Assert.assertEquals(instances.size(), 4);
        assertLabel(instances.get(0), "Sep 2014");
        assertLabel(instances.get(1), "Oct 2014");
        assertLabel(instances.get(2), "Nov 2014");
        assertLabel(instances.get(3), "Dec 2014");


        instances = generator.next();

        print(instances, "Monthly NEXT 1");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "Oct 2014");
        assertLabel(instances.get(1), "Nov 2014");
        assertLabel(instances.get(2), "Dec 2014");
        assertLabel(instances.get(3), "Jan 2015");

        instances = generator.previous();

        print(instances, "Monthly BACK 1");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "Sep 2014");
        assertLabel(instances.get(1), "Oct 2014");
        assertLabel(instances.get(2), "Nov 2014");
        assertLabel(instances.get(3), "Dec 2014");

        instances = generator.next();

        print(instances, "Monthly NEXT 1");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "Oct 2014");
        assertLabel(instances.get(1), "Nov 2014");
        assertLabel(instances.get(2), "Dec 2014");
        assertLabel(instances.get(3), "Jan 2015");

        instances = generator.previous();

        print(instances, "Monthly BACK 1");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "Sep 2014");
        assertLabel(instances.get(1), "Oct 2014");
        assertLabel(instances.get(2), "Nov 2014");
        assertLabel(instances.get(3), "Dec 2014");

        instances = generator.next();

        print(instances, "Monthly NEXT 1");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "Oct 2014");
        assertLabel(instances.get(1), "Nov 2014");
        assertLabel(instances.get(2), "Dec 2014");
        assertLabel(instances.get(3), "Jan 2015");

        instances = generator.previous();

        print(instances, "Monthly BACK 1");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "Sep 2014");
        assertLabel(instances.get(1), "Oct 2014");
        assertLabel(instances.get(2), "Nov 2014");
        assertLabel(instances.get(3), "Dec 2014");

        instances = generator.fullNext();

        print(instances, "Monthly NEXT 4");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "Jan 2015");
        assertLabel(instances.get(1), "Feb 2015");
        assertLabel(instances.get(2), "Mar 2015");
        assertLabel(instances.get(3), "Apr 2015");

        instances = generator.fullPrevious();

        print(instances, "Monthly BACK 4");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "Sep 2014");
        assertLabel(instances.get(1), "Oct 2014");
        assertLabel(instances.get(2), "Nov 2014");
        assertLabel(instances.get(3), "Dec 2014");

    }

    @Test
    public void monthlyForward() {

        PeriodInstanceKeyedGenerator generator = jvmGenerator();
        List<FormInstance> instances = generator.generate(PredefinedPeriods.MONTHLY.getPeriod(), fixedDate(Calendar.DECEMBER, 2014), PeriodInstanceKeyedGenerator.Direction.FORWARD, 4);

        print(instances, "Monthly NEXT 4");

        Assert.assertEquals(instances.size(), 4);
        assertLabel(instances.get(0), "Jan 2015");
        assertLabel(instances.get(1), "Feb 2015");
        assertLabel(instances.get(2), "Mar 2015");
        assertLabel(instances.get(3), "Apr 2015");


    }

    @Test
    public void yearlyBack() {

        PeriodInstanceKeyedGenerator generator = jvmGenerator();
        List<FormInstance> instances = generator.generate(PredefinedPeriods.YEARLY.getPeriod(), fixedDate(Calendar.JANUARY, 2015), PeriodInstanceKeyedGenerator.Direction.BACK, 3);

        print(instances, "Yearly BACK 3");

        Assert.assertEquals(instances.size(), 3);
        assertLabel(instances.get(0), "2012");
        assertLabel(instances.get(1), "2013");
        assertLabel(instances.get(2), "2014");


        instances = generator.next();

        print(instances, "Yearly NEXT 1");

        Assert.assertEquals(instances.size(), 3);

        assertLabel(instances.get(0), "2013");
        assertLabel(instances.get(1), "2014");
        assertLabel(instances.get(2), "2015");

        instances = generator.next();

        print(instances, "Yearly NEXT 1");

        Assert.assertEquals(instances.size(), 3);

        assertLabel(instances.get(0), "2014");
        assertLabel(instances.get(1), "2015");
        assertLabel(instances.get(2), "2016");

        instances = generator.previous();

        print(instances, "Yearly BACK 1");

        Assert.assertEquals(instances.size(), 3);

        assertLabel(instances.get(0), "2013");
        assertLabel(instances.get(1), "2014");
        assertLabel(instances.get(2), "2015");

        instances = generator.previous();

        print(instances, "Yearly BACK 1");

        Assert.assertEquals(instances.size(), 3);

        assertLabel(instances.get(0), "2012");
        assertLabel(instances.get(1), "2013");
        assertLabel(instances.get(2), "2014");

        instances = generator.next();

        print(instances, "Yearly NEXT 1");

        Assert.assertEquals(instances.size(), 3);

        assertLabel(instances.get(0), "2013");
        assertLabel(instances.get(1), "2014");
        assertLabel(instances.get(2), "2015");

        instances = generator.fullNext();

        print(instances, "Yearly NEXT 4");

        Assert.assertEquals(instances.size(), 3);

        assertLabel(instances.get(0), "2016");
        assertLabel(instances.get(1), "2017");
        assertLabel(instances.get(2), "2018");

        instances = generator.fullPrevious();

        print(instances, "Yearly BACK 4");

        Assert.assertEquals(instances.size(), 3);

        assertLabel(instances.get(0), "2013");
        assertLabel(instances.get(1), "2014");
        assertLabel(instances.get(2), "2015");


    }

    @Test
    public void yearlyForward() {

        PeriodInstanceKeyedGenerator generator = jvmGenerator();
        List<FormInstance> instances = generator.generate(PredefinedPeriods.YEARLY.getPeriod(), fixedDate(Calendar.JANUARY, 2014), PeriodInstanceKeyedGenerator.Direction.FORWARD, 3);

        print(instances, "Yearly FORWARD 3");

        Assert.assertEquals(instances.size(), 3);
        assertLabel(instances.get(0), "2015");
        assertLabel(instances.get(1), "2016");
        assertLabel(instances.get(2), "2017");


    }

    @Test
    public void weekly() {
        PeriodInstanceKeyedGenerator generator = jvmGenerator();

        List<FormInstance> instances = generator.generate(PredefinedPeriods.WEEKLY.getPeriod(), fixedDate(Calendar.FEBRUARY), PeriodInstanceKeyedGenerator.Direction.BACK, 4);

        print(instances, "Weekly BACK 4");

        Assert.assertEquals(instances.size(), 4);
        assertLabel(instances.get(0), "2015W1");
        assertLabel(instances.get(1), "2015W2");
        assertLabel(instances.get(2), "2015W3");
        assertLabel(instances.get(3), "2015W4");

        instances = generator.next();

        print(instances, "Weekly NEXT 1");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "2015W2");
        assertLabel(instances.get(1), "2015W3");
        assertLabel(instances.get(2), "2015W4");
        assertLabel(instances.get(3), "2015W5");

        instances = generator.next();

        print(instances, "Weekly NEXT 1");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "2015W3");
        assertLabel(instances.get(1), "2015W4");
        assertLabel(instances.get(2), "2015W5");
        assertLabel(instances.get(3), "2015W6");

        instances = generator.previous();

        print(instances, "Weekly BACK 1");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "2015W2");
        assertLabel(instances.get(1), "2015W3");
        assertLabel(instances.get(2), "2015W4");
        assertLabel(instances.get(3), "2015W5");

        instances = generator.next();

        print(instances, "Weekly NEXT 1");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "2015W3");
        assertLabel(instances.get(1), "2015W4");
        assertLabel(instances.get(2), "2015W5");
        assertLabel(instances.get(3), "2015W6");

        instances = generator.fullPrevious();

        print(instances, "Weekly BACK 4");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "2014W52");
        assertLabel(instances.get(1), "2014W53");
        assertLabel(instances.get(2), "2015W1");
        assertLabel(instances.get(3), "2015W2");
    }

    @Test
    public void daily() {
        PeriodInstanceKeyedGenerator generator = jvmGenerator();

        List<FormInstance> instances = generator.generate(PredefinedPeriods.DAILY.getPeriod(), fixedDate(4, Calendar.FEBRUARY, 2016), PeriodInstanceKeyedGenerator.Direction.BACK, 4);

        print(instances, "Daily BACK 4");

        Assert.assertEquals(instances.size(), 4);
        assertLabel(instances.get(0), "31 Jan 2016");
        assertLabel(instances.get(1), "01 Feb 2016");
        assertLabel(instances.get(2), "02 Feb 2016");
        assertLabel(instances.get(3), "03 Feb 2016");

        instances = generator.previous();

        print(instances, "Daily PREVIOUS 1");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "30 Jan 2016");
        assertLabel(instances.get(1), "31 Jan 2016");
        assertLabel(instances.get(2), "01 Feb 2016");
        assertLabel(instances.get(3), "02 Feb 2016");

        instances = generator.previous();

        print(instances, "Daily PREVIOUS 1");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "29 Jan 2016");
        assertLabel(instances.get(1), "30 Jan 2016");
        assertLabel(instances.get(2), "31 Jan 2016");
        assertLabel(instances.get(3), "01 Feb 2016");

        instances = generator.previous();

        print(instances, "Daily PREVIOUS 1");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "28 Jan 2016");
        assertLabel(instances.get(1), "29 Jan 2016");
        assertLabel(instances.get(2), "30 Jan 2016");
        assertLabel(instances.get(3), "31 Jan 2016");

        instances = generator.previous();

        print(instances, "Daily PREVIOUS 1");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "27 Jan 2016");
        assertLabel(instances.get(1), "28 Jan 2016");
        assertLabel(instances.get(2), "29 Jan 2016");
        assertLabel(instances.get(3), "30 Jan 2016");

        instances = generator.next();

        print(instances, "Daily NEXT 1");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "28 Jan 2016");
        assertLabel(instances.get(1), "29 Jan 2016");
        assertLabel(instances.get(2), "30 Jan 2016");
        assertLabel(instances.get(3), "31 Jan 2016");

        instances = generator.next();

        print(instances, "Daily NEXT 1");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "29 Jan 2016");
        assertLabel(instances.get(1), "30 Jan 2016");
        assertLabel(instances.get(2), "31 Jan 2016");
        assertLabel(instances.get(3), "01 Feb 2016");

    }

    @Test
    public void fortnightly() {
        PeriodInstanceKeyedGenerator generator = jvmGenerator();

        List<FormInstance> instances = generator.generate(PredefinedPeriods.BI_WEEKLY.getPeriod(), fixedDate(4, Calendar.FEBRUARY, 2016), PeriodInstanceKeyedGenerator.Direction.BACK, 4);

        print(instances, "fortnightly BACK 4");

        Assert.assertEquals(instances.size(), 4);
        assertLabel(instances.get(0), "2015W49-50");
        assertLabel(instances.get(1), "2015W51-52");
        assertLabel(instances.get(2), "2016W1-2");
        assertLabel(instances.get(3), "2016W3-4");

        instances = generator.previous();

        print(instances, "Daily PREVIOUS 1");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "2015W47-48");
        assertLabel(instances.get(1), "2015W49-50");
        assertLabel(instances.get(2), "2015W51-52");
        assertLabel(instances.get(3), "2016W1-2");

        instances = generator.previous();

        print(instances, "Daily PREVIOUS 1");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "2015W45-46");
        assertLabel(instances.get(1), "2015W47-48");
        assertLabel(instances.get(2), "2015W49-50");
        assertLabel(instances.get(3), "2015W51-52");

        instances = generator.previous();

        print(instances, "Daily PREVIOUS 1");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "2015W43-44");
        assertLabel(instances.get(1), "2015W45-46");
        assertLabel(instances.get(2), "2015W47-48");
        assertLabel(instances.get(3), "2015W49-50");

        instances = generator.previous();

        print(instances, "Daily PREVIOUS 1");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "2015W41-42");
        assertLabel(instances.get(1), "2015W43-44");
        assertLabel(instances.get(2), "2015W45-46");
        assertLabel(instances.get(3), "2015W47-48");

        instances = generator.next();

        print(instances, "Daily NEXT 1");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "2015W43-44");
        assertLabel(instances.get(1), "2015W45-46");
        assertLabel(instances.get(2), "2015W47-48");
        assertLabel(instances.get(3), "2015W49-50");

        instances = generator.next();

        print(instances, "Daily NEXT 1");

        Assert.assertEquals(instances.size(), 4);

        assertLabel(instances.get(0), "2015W45-46");
        assertLabel(instances.get(1), "2015W47-48");
        assertLabel(instances.get(2), "2015W49-50");
        assertLabel(instances.get(3), "2015W51-52");

    }

    private PeriodInstanceKeyedGenerator jvmGenerator() {
        return new PeriodInstanceKeyedGenerator(ResourceId.generateId(), new PeriodInstanceKeyedGenerator.Formatter() {
            @Override
            public String format(String pattern, Date date) {
                return new SimpleDateFormat(pattern).format(date);
            }
        }, jvmDayOfWeekProvider());
    }

    public static CalendarUtils.DayOfWeekProvider jvmDayOfWeekProvider() {
        return new CalendarUtils.DayOfWeekProvider() {
            @Override
            public DayOfWeek dayOfWeek(Date date) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                return DayOfWeek.fromValue(calendar.get(Calendar.DAY_OF_WEEK) - 1);
            }
        };
    }

    private Date fixedDate(int month) {
        return fixedDate(month, 2015);
    }

    private Date fixedDate(int month, int year) {
        return fixedDate(1, month, year);
    }

    public static Date fixedDate(int day, int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        System.out.println(calendar.getTime());
        return calendar.getTime();
    }

    private static void assertLabel(FormInstance instance, String expectedLabel) {
        Assert.assertEquals(FormInstanceLabeler.getLabel(instance), expectedLabel);
    }

    private void print(List<FormInstance> instances) {
        print(instances, "");
    }

    private void print(List<FormInstance> instances, String title) {
        System.out.println("\n :" + title);
        for (FormInstance instance : instances) {
            DateRange range = PeriodInstanceKeyedGenerator.getDateRangeFromInstance(instance);
            System.out.println("start=" + new LocalDate(range.getStart()) + ", end=" + new LocalDate(range.getEnd()) +
                    ", label=" + FormInstanceLabeler.getLabel(instance) + ", id=" + instance.getId());
        }
    }
}
