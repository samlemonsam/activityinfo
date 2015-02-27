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

import org.activityinfo.model.date.DateRange;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.FormInstanceLabeler;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.period.PredefinedPeriods;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.ui.client.component.form.subform.InstanceGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author yuriyz on 02/05/2015.
 */
public class InstanceGeneratorTest {

    @Test
    public void monthlyStateBugWithNextNextBack() {
        InstanceGenerator generator = jvmGenerator();

        List<FormInstance> instances = generator.generate(PredefinedPeriods.MONTHLY.getPeriod(), fixedDate(Calendar.FEBRUARY), InstanceGenerator.Direction.BACK, 4);

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

        InstanceGenerator generator = jvmGenerator();
        List<FormInstance> instances = generator.generate(PredefinedPeriods.MONTHLY.getPeriod(), fixedDate(Calendar.JANUARY), InstanceGenerator.Direction.BACK, 4);

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

        InstanceGenerator generator = jvmGenerator();
        List<FormInstance> instances = generator.generate(PredefinedPeriods.MONTHLY.getPeriod(), fixedDate(Calendar.DECEMBER, 2014), InstanceGenerator.Direction.FORWARD, 4);

        print(instances, "Monthly NEXT 4");

        Assert.assertEquals(instances.size(), 4);
        assertLabel(instances.get(0), "Jan 2015");
        assertLabel(instances.get(1), "Feb 2015");
        assertLabel(instances.get(2), "Mar 2015");
        assertLabel(instances.get(3), "Apr 2015");


    }

    @Test
    public void yearlyBack() {

        InstanceGenerator generator = jvmGenerator();
        List<FormInstance> instances = generator.generate(PredefinedPeriods.YEARLY.getPeriod(), fixedDate(Calendar.JANUARY, 2015), InstanceGenerator.Direction.BACK, 3);

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

        InstanceGenerator generator = jvmGenerator();
        List<FormInstance> instances = generator.generate(PredefinedPeriods.YEARLY.getPeriod(), fixedDate(Calendar.JANUARY, 2014), InstanceGenerator.Direction.FORWARD, 3);

        print(instances, "Yearly FORWARD 3");

        Assert.assertEquals(instances.size(), 3);
        assertLabel(instances.get(0), "2015");
        assertLabel(instances.get(1), "2016");
        assertLabel(instances.get(2), "2017");


    }

    private InstanceGenerator jvmGenerator() {
        return new InstanceGenerator(ResourceId.generateId(), new InstanceGenerator.Formatter() {
            @Override
            public String format(String pattern, Date date) {
                return new SimpleDateFormat(pattern).format(date);
            }
        });
    }

    private Date fixedDate(int month) {
        return fixedDate(month, 2015);
    }

    private Date fixedDate(int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        System.out.println(calendar.getTime());
        return calendar.getTime();
    }

    private void assertLabel(FormInstance instance, String expectedLabel) {
        Assert.assertEquals(FormInstanceLabeler.getLabel(instance), expectedLabel);
    }

    private void print(List<FormInstance> instances) {
        print(instances, "");
    }

    private void print(List<FormInstance> instances, String title) {
        System.out.println("\n :" + title);
        for (FormInstance instance : instances) {
            DateRange range = InstanceGenerator.getDateRangeFromInstance(instance);
            System.out.println("start=" + new LocalDate(range.getStart()) + ", end=" + new LocalDate(range.getEnd()) +
                    ", label=" + FormInstanceLabeler.getLabel(instance) + ", id=" + instance.getId());
        }
    }
}
