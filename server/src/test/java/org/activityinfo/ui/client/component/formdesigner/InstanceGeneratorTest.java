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

import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.FormInstanceLabeler;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.period.PredefinedPeriods;
import org.activityinfo.ui.client.component.formdesigner.container.InstanceGenerator;
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
    public void monthlyBack() {

        InstanceGenerator generator = jvmGenerator();
        List<FormInstance> instances = generator.generate(PredefinedPeriods.MONTHLY.getPeriod(), fixedDate(), InstanceGenerator.Direction.BACK, 4);

        Assert.assertEquals(instances.size(), 4);
        assertLabel(instances.get(0), "Dec 2014");
        assertLabel(instances.get(1), "Nov 2014");
        assertLabel(instances.get(2), "Oct 2014");
        assertLabel(instances.get(3), "Sep 2014");

        print(instances);
    }

    @Test
    public void monthlyForward() {

        InstanceGenerator generator = jvmGenerator();
        List<FormInstance> instances = generator.generate(PredefinedPeriods.MONTHLY.getPeriod(), fixedDate(), InstanceGenerator.Direction.FORWARD, 4);

        Assert.assertEquals(instances.size(), 4);
        assertLabel(instances.get(0), "Feb 2015");
        assertLabel(instances.get(1), "Mar 2015");
        assertLabel(instances.get(2), "Apr 2015");
        assertLabel(instances.get(3), "May 2015");

        print(instances);
    }

    @Test
    public void yearlyBack() {

        InstanceGenerator generator = jvmGenerator();
        List<FormInstance> instances = generator.generate(PredefinedPeriods.YEARLY.getPeriod(), fixedDate(), InstanceGenerator.Direction.BACK, 3);

        Assert.assertEquals(instances.size(), 3);
        assertLabel(instances.get(0), "2014");
        assertLabel(instances.get(1), "2013");
        assertLabel(instances.get(2), "2012");

        print(instances);
    }

    @Test
    public void yearlyForward() {

        InstanceGenerator generator = jvmGenerator();
        List<FormInstance> instances = generator.generate(PredefinedPeriods.YEARLY.getPeriod(), fixedDate(), InstanceGenerator.Direction.FORWARD, 3);

        Assert.assertEquals(instances.size(), 3);
        assertLabel(instances.get(0), "2016");
        assertLabel(instances.get(1), "2017");
        assertLabel(instances.get(2), "2018");

        print(instances);
    }

    private InstanceGenerator jvmGenerator() {
        return new InstanceGenerator(ResourceId.generateId(), new InstanceGenerator.Formatter() {
            @Override
            public String format(String pattern, Date date) {
                return new SimpleDateFormat(pattern).format(date);
            }
        });
    }

    private Date fixedDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2015);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        System.out.println(calendar.getTime());
        return calendar.getTime();
    }

    private void assertLabel(FormInstance instance, String expectedLabel) {
        Assert.assertEquals(FormInstanceLabeler.getLabel(instance), expectedLabel);
    }

    private void print(List<FormInstance> instances) {
        for (FormInstance instance : instances) {
            System.out.println(instance);
        }
    }
}
