package org.activityinfo.model.expr;
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

import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.shared.adapter.ActivityFormClassBuilder;
import org.activityinfo.legacy.shared.adapter.ActivityFormLockBuilder;
import org.activityinfo.legacy.shared.command.GetActivityForm;
import org.activityinfo.legacy.shared.model.ActivityDTO;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.legacy.shared.model.LockedPeriodDTO;
import org.activityinfo.legacy.shared.model.ProjectDTO;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.legacy.BuiltinFields;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.lock.LockEvaluator;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.server.command.CommandTestCase2;
import org.activityinfo.server.database.OnDataSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

/**
 * @author yuriyz on 10/09/2015.
 */
@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/schema2.db.xml")
public class LockEvaluatorTest extends CommandTestCase2 {

    private static final int ACTIVITY_ID = 1;

    private FormClass formClass;

    @Before
    public void setUp() {
        formClass = createFormClass();
    }

    @Test
    public void lock() {
        assertLocked(instanceWithEndDate(new LocalDate(2009, 1, 1)), true);
        assertLocked(instanceWithEndDate(new LocalDate(2009, 1, 2)), true);
        assertLocked(instanceWithEndDate(new LocalDate(2009, 1, 22)), true);

        assertLocked(instanceWithEndDate(new LocalDate(2008, 1, 1)), false);
        assertLocked(instanceWithEndDate(new LocalDate(2009, 1, 23)), false);
        assertLocked(instanceWithEndDate(new LocalDate(2014, 1, 1)), false);
        assertLocked(instanceWithEndDate(new LocalDate(2014, 1, 3)), false);
    }

    @Test
    public void projectLock() {
        int projectId = 1;

        LockedPeriodDTO lock = new LockedPeriodDTO();
        lock.setId(11);
        lock.setEnabled(true);
        lock.setName("Lock");
        lock.setParentId(projectId);
        lock.setParentType(ProjectDTO.ENTITY_NAME);
        lock.setFromDate(new com.bedatadriven.rebar.time.calendar.LocalDate(2015, 1, 1));
        lock.setToDate(new com.bedatadriven.rebar.time.calendar.LocalDate(2015, 10, 1));

        formClass.getLocks().add(ActivityFormLockBuilder.fromLock(lock, formClass.getId()));

        assertLocked(instanceWithEndDate(new LocalDate(2009, 1, 1), projectId), true);
        assertLocked(instanceWithEndDate(new LocalDate(2009, 1, 2), projectId), true);
        assertLocked(instanceWithEndDate(new LocalDate(2009, 1, 22), projectId), true);
        assertLocked(instanceWithEndDate(new LocalDate(2015, 1, 1), projectId), true);
        assertLocked(instanceWithEndDate(new LocalDate(2015, 3, 3), projectId), true);

        assertLocked(instanceWithEndDate(new LocalDate(2008, 1, 1), projectId), false);
        assertLocked(instanceWithEndDate(new LocalDate(2009, 1, 23), projectId), false);
        assertLocked(instanceWithEndDate(new LocalDate(2014, 1, 1), projectId), false);
        assertLocked(instanceWithEndDate(new LocalDate(2014, 1, 3), projectId), false);
        assertLocked(instanceWithEndDate(new LocalDate(2015, 10, 10), projectId), false);
        assertLocked(instanceWithEndDate(new LocalDate(2015, 10, 10), 2), false);
        assertLocked(instanceWithEndDate(new LocalDate(2015, 10, 10), 3), false);
    }

    private FormInstance instanceWithEndDate(LocalDate endDate) {
        return instanceWithEndDate(endDate, -1);
    }

    private FormInstance instanceWithEndDate(LocalDate endDate, int projectId) {
        FormInstance instance = new FormInstance(ResourceId.generateId(), formClass.getId());
        instance.set(BuiltinFields.getStartDateField(formClass).getId(), new LocalDate(2015, 1, 1));
        instance.set(BuiltinFields.getEndDateField(formClass).getId(), endDate);

        if (projectId != -1) {
            instance.set(CuidAdapter.field(formClass.getId(), CuidAdapter.PROJECT_FIELD), new ReferenceValue(CuidAdapter.projectInstanceId(projectId)));
        }
        return instance;
    }

    private void assertLocked(FormInstance instance, boolean expected) {
        assertEquals(expected, new LockEvaluator(formClass).isLocked(instance));
    }

    private FormClass createFormClass() {
        LockedPeriodDTO lock = new LockedPeriodDTO();
        lock.setId(1);
        lock.setEnabled(true);
        lock.setName("Lock");
        lock.setParentId(ACTIVITY_ID);
        lock.setParentType(ActivityDTO.ENTITY_NAME);
        lock.setFromDate(new com.bedatadriven.rebar.time.calendar.LocalDate(2015, 1, 1));
        lock.setToDate(new com.bedatadriven.rebar.time.calendar.LocalDate(2015, 10, 1));

        ActivityFormDTO activityFormDTO = execute(new GetActivityForm(1));
        activityFormDTO.setId(ACTIVITY_ID);
        activityFormDTO.setName("Activity");
        activityFormDTO.setDatabaseId(1);
        activityFormDTO.getLockedPeriods().add(lock);
        return new ActivityFormClassBuilder(activityFormDTO).build();
    }



}
