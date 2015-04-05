package org.activityinfo.server.command;

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

import com.google.common.collect.Maps;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.fixtures.MockHibernateModule;
import org.activityinfo.fixtures.Modules;
import org.activityinfo.legacy.shared.command.*;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.model.ActivityDTO;
import org.activityinfo.legacy.shared.model.IndicatorDTO;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.server.endpoint.gwtrpc.GwtRpcModule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(InjectionSupport.class)
@Modules({
        MockHibernateModule.class,
        GwtRpcModule.class,
})
@OnDataSet("/dbunit/sites-simple1.db.xml")
public class LocalSchemaChangeTest extends LocalHandlerTestCase {

    @Test
    public void updateIndicator() {

        synchronizeFirstTime();

        Map<String, Object> changes = Maps.newHashMap();
        changes.put("name", "New Name");

        UpdateEntity update = new UpdateEntity("Indicator", 5, changes);

        executeRemotely(update);

        synchronize();

        SchemaDTO schema = executeLocally(new GetSchema());

        assertThat(schema.getIndicatorById(5).getName(), equalTo("New Name"));
    }

    @Test
    public void createActivity() {

        synchronizeFirstTime();

        SchemaDTO schema = executeLocally(new GetSchema());

        ActivityDTO activity = new ActivityDTO();
        activity.setName("New Activity");
        activity.setReportingFrequency(0);
        activity.setLocationType(schema.getLocationTypeById(1));

        CreateResult createResult = executeRemotely(CreateEntity.Activity(
                schema.getDatabaseById(1), activity));

        synchronize();

        schema = executeLocally(new GetSchema());

        ActivityDTO createdActivity = schema.getActivityById(createResult
                .getNewId());

        assertThat(createdActivity, is(not(nullValue())));
        assertThat(createdActivity.getName(), equalTo(activity.getName()));
    }

    @Test
    public void createIndicator() {

        synchronizeFirstTime();

        SchemaDTO schema = executeLocally(new GetSchema());

        Map<String, Object> indicator = Maps.newHashMap();
        indicator.put("name", "New Indicator");
        indicator.put("units", "bricks");
        indicator.put("activityId", 2);

        CreateResult createResult = executeRemotely(new CreateEntity(
                "Indicator", indicator));

        synchronize();

        schema = executeLocally(new GetSchema());

        IndicatorDTO createdIndicator = schema.getIndicatorById(createResult
                .getNewId());

        assertThat(createdIndicator, is(not(nullValue())));
        assertThat(createdIndicator.getName(), equalTo("New Indicator"));
    }

    @Test
    public void deleteActivity() {

        synchronizeFirstTime();

        SchemaDTO schema = executeLocally(new GetSchema());
        assertThat(schema.getActivityById(2), is(not(nullValue())));

        executeRemotely(new Delete("Activity", 2));

        synchronize();

        schema = executeLocally(new GetSchema());

        assertThat(schema.getActivityById(2), is(nullValue()));
    }

    @Test
    public void updateAttribute() {

        Map<String, Object> changes = Maps.newHashMap();
        changes.put("name", "New Name");
    }

}
