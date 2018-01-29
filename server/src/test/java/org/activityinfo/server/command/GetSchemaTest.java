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

import com.bedatadriven.rebar.sql.server.jdbc.JdbcScheduler;
import com.bedatadriven.rebar.time.calendar.LocalDate;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.shared.command.*;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.promise.Promise;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.server.endpoint.rest.SchemaCsvWriter;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.activityinfo.promise.PromiseMatchers.resolvesTo;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/sites-simple1.db.xml")
public class GetSchemaTest extends CommandTestCase2 {

    
    @Before
    public void cleanUpScheduler() {
        JdbcScheduler.get().forceCleanup();
    }

    @Test
    public void testDatabaseVisibilityForOwners() throws CommandException {

        // owners should be able to see their databases

        setUser(1); // Alex

        SchemaDTO schema = execute(new GetSchema());

        assertThat("database count", schema.getDatabases(), hasSize(3));
        assertThat("database list is sorted", schema.getDatabases().get(0).getName(), equalTo("Alpha"));

        assertTrue("ALEX(owner) in PEAR", schema.getDatabaseById(1) != null); // PEAR
        assertTrue("ALEX can design", schema.getDatabaseById(1).isDesignAllowed());
        assertTrue("Alex can edit all", schema.getDatabaseById(1).isEditAllowed());
        assertTrue("object graph is preserved",
                schema.getDatabaseById(1).getCountry() ==
                schema.getDatabaseById(2).getCountry());

        assertThat(schema.getDatabaseById(1).getFolders(), hasSize(1));

        FolderDTO nfiFolder = schema.getDatabaseById(1).getFolders().get(0);
        assertThat(nfiFolder.getName(), equalTo("NFI Cluster"));
        assertThat(nfiFolder.getActivities(), hasSize(1));


        ActivityDTO nfi = schema.getDatabaseById(1).getActivities().get(0);
        assertThat(nfi.getLocationTypeId(), equalTo(1));
        assertThat(nfi.<Integer>get("locationTypeId"), equalTo(1));

        AdminLevelDTO adminLevel = schema.getCountries().get(0).getAdminLevels().get(0);
        assertThat("CountryId is not null", adminLevel.getCountryId(), not(equalTo(0)));
        assertThat("CountryId is not null", adminLevel.getId(), not(equalTo(0)));

        assertTrue("CountryId is not null",
                schema.getCountries().get(0).getAdminLevels().get(0).getCountryId() != 0);

        ActivityFormDTO nfiForm = execute(new GetActivityForm(nfi.getId()));

        assertThat("deleted attribute is not present", nfiForm.getAttributeGroups(), hasSize(3));
    }

    @Test
    @OnDataSet("/dbunit/sites-public.db.xml")
    public void testDatabasePublished() throws CommandException {

        // Anonymouse user should fetch schema database with published
        // activities.
        setUser(0);

        SchemaDTO schema = execute(new GetSchema());

        assertThat(schema.getDatabases().size(), equalTo(1));
    }

    @Test
    public void testLockedProjects() {
        setUser(1);
        SchemaDTO schema = execute(new GetSchema());

        assertThat(schema.getProjectById(1).getLockedPeriods().size(),
                equalTo(1));

        LockedPeriodSet locks = new LockedPeriodSet(schema);
        assertTrue(locks.isProjectLocked(1, new LocalDate(2009, 1, 1)));
        assertTrue(locks.isProjectLocked(1, new LocalDate(2009, 1, 6)));
        assertTrue(locks.isProjectLocked(1, new LocalDate(2009, 1, 12)));
        assertFalse(locks.isProjectLocked(1, new LocalDate(2008, 1, 12)));
        assertFalse(locks.isProjectLocked(1, new LocalDate(2010, 1, 12)));

    }

    @Test
    public void testDatabaseVisibilityForView() throws CommandException {

        setUser(2); // Bavon

        SchemaDTO schema = execute(new GetSchema());

        assertThat(schema.getDatabases().size(), equalTo(2));
        assertThat("BAVON in PEAR", schema.getDatabaseById(1), is(not(nullValue())));
        assertThat(schema.getDatabaseById(1).getMyPartnerId(), equalTo(1));
        assertThat(schema.getDatabaseById(1).isEditAllowed(), equalTo(true));
        assertThat(schema.getDatabaseById(1).isEditAllAllowed(), equalTo(false));
    }

    @Test
    public void testDatabaseVisibilityNone() throws CommandException {
        setUser(3); // Stefan

        SchemaDTO schema = execute(new GetSchema());

        assertTrue("STEFAN does not have access to RRM", schema.getDatabaseById(2) == null);
    }

    @Test
    public void testIndicators() throws CommandException {

        setUser(1); // Alex

        ActivityFormDTO schema = execute(new GetActivityForm(2));

        assertThat("no indicators case", schema.getIndicators(), hasSize(0));

        ActivityFormDTO nfi = execute(new GetActivityForm(1));

        assertThat("indicators are present", nfi.getIndicators(), hasSize(5));

        IndicatorDTO test = nfi.getIndicatorById(2);
        assertThat(test, hasProperty("name", equalTo("baches")));
        assertThat(test, hasProperty("aggregation", equalTo(IndicatorDTO.AGGREGATE_SUM)));
        assertThat(test, hasProperty("category", equalTo("outputs")));
        assertThat(test, hasProperty("listHeader", equalTo("header")));
        assertThat(test, hasProperty("description", equalTo("desc")));
    }

    @Test
    public void testAttributes() throws CommandException {

        setUser(1); // Alex

        ActivityFormDTO form = execute(new GetActivityForm(3));
        assertThat("no attributes case", form.getAttributeGroups(), hasSize(0));

        ActivityFormDTO nfi = execute(new GetActivityForm(1));
        AttributeGroupDTO group = nfi.getAttributeGroupById(1);
        assertThat(group, notNullValue());
        assertThat("attributes are present", group.getAttributes(), hasSize(2));

        AttributeDTO test = nfi.getAttributeById(1);

        assertThat(test, hasProperty("name", equalTo("Catastrophe Naturelle")));
    }

    @Test
    @OnDataSet("/dbunit/schema3.db.xml")
    public void updateFolderName() {
        setUser(1);

        Map<String, Object> changes = new HashMap<>();
        changes.put("name", "New Name");

        UpdateEntity update = new UpdateEntity("Folder", 1, changes);

        execute(update);

        SchemaDTO schema = execute(new GetSchema());

        FolderDTO folder = schema.getDatabaseById(1).getFolderById(1);

        assertThat(folder.getName(), equalTo("New Name"));
    }

    @Test
    @OnDataSet("/dbunit/schema3.db.xml")
    public void createFolder() {
        setUser(1);

        CreateResult result = execute(new CreateEntity(new FolderDTO(1, "NFI")));

        SchemaDTO schema = execute(new GetSchema());

        FolderDTO folder = schema.getDatabaseById(1).getFolderById(result.getNewId());

        assertThat(folder.getName(), equalTo("NFI"));
    }

    @Test
    @OnDataSet("/dbunit/schema3.db.xml")
    public void folderLimitedPermissions() {
        setUser(1);

        int databaseId = 1;
        int healthFolderId = 3;
        int bavonUserId = 2;

        // Add bavon, but only give him access to the education folder
        UserPermissionDTO bavon = new UserPermissionDTO();
        bavon.setEmail("bavon@nrc.org");
        bavon.setPartner(new PartnerDTO(1, "NRC"));
        bavon.setAllowView(true);
        bavon.setFolders(Arrays.asList(new FolderDTO(databaseId, healthFolderId, "Health")));

        execute(new UpdateUserPermissions(databaseId, bavon));

        setUser(bavonUserId);

        SchemaDTO schema = execute(new GetSchema());
        UserDatabaseDTO database = schema.getDatabaseById(databaseId);
        assertThat(database.getFolders(), hasSize(1));
        assertThat(database.getActivities(), hasSize(1));
    }

    @Test
    @OnDataSet("/dbunit/schema3.db.xml")
    public void moveActivityToFolder() {
        setUser(1);

        int legalActivityId = 1;
        int educationFolderId = 3;


        Map<String, Object> changes = new HashMap<>();
        changes.put("folderId", educationFolderId);

        execute(new UpdateEntity(ActivityDTO.ENTITY_NAME, legalActivityId, changes));


        SchemaDTO schema = execute(new GetSchema());
        FolderDTO folder = schema.getDatabaseById(1).getFolderById(educationFolderId);

        assertThat(folder.getActivities(), hasSize(2));

        ActivityDTO activity = schema.getActivityById(legalActivityId);
        assertThat(activity.getFolder().getName(), equalTo("Education"));

    }

    @Test
    public void toCSV() throws IOException {
        int databaseId = 1;

        SchemaCsvWriter writer = new SchemaCsvWriter(getDispatcherSync());
        writer.write(databaseId);

        System.out.println(writer.toString());
    }

    @Test
    public void newApiTest() {


        Promise<FormClass> userForm = locator.getFormClass(CuidAdapter.activityFormClass(1));

        assertThat(userForm, resolvesTo(CoreMatchers.<FormClass>notNullValue()));
    }


}
