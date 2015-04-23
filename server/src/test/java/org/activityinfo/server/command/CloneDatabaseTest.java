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

import com.extjs.gxt.ui.client.data.BaseModelData;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.shared.command.CloneDatabase;
import org.activityinfo.legacy.shared.command.GetActivityForm;
import org.activityinfo.legacy.shared.command.GetFormClass;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormElement;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormSection;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.server.database.OnDataSet;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author yuriyz on 11/26/2014.
 */
@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/clone-database.db.xml")
public class CloneDatabaseTest extends CommandTestCase2 {

    public static final int PEAR_DATABASE_ID = 1;
    public static final int UKRAINE_COUNTRY_ID = 2;

    @Test
    public void fullClone() throws CommandException {

        SchemaDTO schema = execute(new GetSchema());
        UserDatabaseDTO pearDb = schema.getDatabaseById(PEAR_DATABASE_ID);

        CloneDatabase cloneDatabase = new CloneDatabase()
                .setSourceDatabaseId(pearDb.getId())
                .setCopyData(true)
                .setCopyPartners(true)
                .setCopyUserPermissions(true)
                .setCountryId(pearDb.getCountry().getId())
                .setName("PearClone")
                .setDescription("PearClone Description");

        CreateResult cloneResult = execute(cloneDatabase);
        assertNotEquals(cloneResult.getNewId(), pearDb.getId());

        assertDbCloned(cloneResult.getNewId(), pearDb.getId(), cloneDatabase);

        // todo assert data copy
    }

    @Test
    public void cloneWithoutDataCopy() throws CommandException {

        SchemaDTO schema = execute(new GetSchema());
        UserDatabaseDTO pearDb = schema.getDatabaseById(PEAR_DATABASE_ID);

        CloneDatabase cloneDatabase = new CloneDatabase()
                .setSourceDatabaseId(pearDb.getId())
                .setCopyData(false)
                .setCopyPartners(true)
                .setCopyUserPermissions(true)
                .setCountryId(pearDb.getCountry().getId())
                .setName("PearClone")
                .setDescription("PearClone Description");

        CreateResult cloneResult = execute(cloneDatabase);
        assertNotEquals(cloneResult.getNewId(), pearDb.getId());

        assertDbCloned(cloneResult.getNewId(), pearDb.getId(), cloneDatabase);
    }

    // https://bedatadriven.atlassian.net/browse/AI-315
//    it seems that the problem occurs when there is a classic form with a "Village" location. If you change countries,
//    you need to change the locationTypeId to a location type in the new country. Users can change the location type afterwards,
//    so I would suggest that we apply a simple rule:
//
//    1. If there is a location type with the same name in the new country, use that location Type
//    2. if the source locationtype is bound to an adminlevel, choose the first root adminlevel in the new country
//    3. If the source locationtype is the null location type ( = Country) then the use the corresponding null locationtype in the new form
//    4. Otherwise use the "Village" location type in the target country.
    @Test
    public void cloneWithDifferentCountry() throws CommandException {

        SchemaDTO schema = execute(new GetSchema());
        UserDatabaseDTO pearDb = schema.getDatabaseById(PEAR_DATABASE_ID);

        CloneDatabase cloneDatabase = new CloneDatabase()
                .setSourceDatabaseId(pearDb.getId())
                .setCopyData(false)
                .setCopyPartners(true)
                .setCopyUserPermissions(true)
                .setCountryId(UKRAINE_COUNTRY_ID)
                .setName("PearClone")
                .setDescription("PearClone Description");

        CreateResult cloneResult = execute(cloneDatabase);
        assertNotEquals(cloneResult.getNewId(), pearDb.getId());

        assertDbCloned(cloneResult.getNewId(), pearDb.getId(), cloneDatabase);
    }

    private UserDatabaseDTO assertDbCloned(int newDbId, int sourceDbId, CloneDatabase cloneDatabase) {
        assertNotEquals(newDbId, sourceDbId);

        SchemaDTO schema = execute(new GetSchema());

        UserDatabaseDTO sourceDb = schema.getDatabaseById(sourceDbId);
        UserDatabaseDTO targetDb = schema.getDatabaseById(newDbId);

        assertEquals(targetDb.getName(), "PearClone");
        assertEquals(targetDb.getFullName(), "PearClone Description");

        assertFormClassesCloned(sourceDb, targetDb);

        if (cloneDatabase.isCopyPartners()) {
            assertPropertyForEach(sourceDb.getPartners(), targetDb.getPartners(),
                    "name", "fullName");
        }
        if (cloneDatabase.isCopyUserPermissions()) {
            // todo
        }
        if (cloneDatabase.isCopyData()) {
            // todo assert data
        }
        return targetDb;
    }

    private void assertFormClassesCloned(UserDatabaseDTO sourceDb, UserDatabaseDTO targetDb) {
        assertEquals(sourceDb.getActivities().size(), targetDb.getActivities().size());
        for (ActivityDTO activityDTO : sourceDb.getActivities()) {
            ActivityFormDTO sourceActivity = execute(new GetActivityForm(activityDTO.getId()));
            ActivityFormDTO targetActivity = execute(new GetActivityForm(entityByName(targetDb.getActivities(), sourceActivity.getName()).getId()));

            // legacy level
            assertActivityClone(sourceActivity, targetActivity);

            // form class level
            FormClass sourceFormClass = execute(new GetFormClass(sourceActivity.getResourceId())).getFormClass();
            FormClass targetFormClass = execute(new GetFormClass(targetActivity.getResourceId())).getFormClass();
            assertFormClass(sourceFormClass, targetFormClass);
        }
    }

    private void assertActivityClone(ActivityFormDTO sourceActivity, ActivityFormDTO targetActivity) {
        assertProperties(sourceActivity, targetActivity,
                "name", "category", "classicView");

        // indicators
        assertPropertyForEach(sourceActivity.getIndicators(), targetActivity.getIndicators(),
                "name", "units", "expression", "skipExpression", "nameInExpression", "calculatedAutomatically");

        // attributes groups
        for (AttributeGroupDTO sourceAttributeGroup : sourceActivity.getAttributeGroups()) {
            AttributeGroupDTO targetGroup = entityByName(targetActivity.getAttributeGroups(), sourceAttributeGroup.getName());

            assertProperties(sourceAttributeGroup, targetGroup,
                    "name", "mandatory", "defaultValue", "workflow", "multipleAllowed");

            // attributes
            assertPropertyForEach(sourceAttributeGroup.getAttributes(), targetGroup.getAttributes(), "name");
        }
    }

    private void assertFormClass(FormClass sourceFormClass, FormClass targetFormClass) {
        assertNotEquals(sourceFormClass.getId(), targetFormClass.getId());

        assertEquals(sourceFormClass.getLabel(), targetFormClass.getLabel());
        assertEquals(sourceFormClass.getDescription(), targetFormClass.getDescription());

        // fields
        for (FormField sourceField : sourceFormClass.getFields()) {
            FormField targetField = (FormField) elementByName(targetFormClass.getElements(), sourceField.getLabel());

            assertNotEquals(sourceField.getId(), targetField.getId());
            assertEquals(sourceField.getDescription(), targetField.getDescription());
            assertEquals(sourceField.getCode(), targetField.getCode());
            assertEquals(sourceField.getRelevanceConditionExpression(), targetField.getRelevanceConditionExpression());

            assertEquals(sourceField.getType().getTypeClass(), targetField.getType().getTypeClass());

            // todo
            if (sourceField.getType() instanceof ReferenceType) {
                // need something more sophisticated to check equality of ReferenceType
            } else if (sourceField.getType() instanceof EnumType) {
                // need something more sophisticated to check equality of ReferenceType
            }
        }

        // sections
        for (FormSection sourceSection : sourceFormClass.getSections()) {
            FormSection targetSection = (FormSection) elementByName(targetFormClass.getElements(), sourceSection.getLabel());

            assertNotEquals(sourceSection.getId(), targetSection.getId());
            assertEquals(sourceSection.getLabel(), targetSection.getLabel());
        }
    }

    private static <T extends BaseModelData> void assertPropertyForEach(List<T> sourceList, List<T> targetList, String... properties) {
        assertEquals(sourceList.size(), targetList.size());
        for (T sourceItem : sourceList) {
            T targetTarget = entityByName(targetList, (String) sourceItem.get("name"));

            assertProperties(sourceItem, targetTarget, properties);
        }
    }

    private static <T extends BaseModelData> void assertProperties(T entity1, T entity2, String... propertyNames) {
        if (propertyNames == null || propertyNames.length == 0) {
            throw new RuntimeException("No property names specified.");
        }

        assertNotEquals(entity1.get("id"), entity2.get("id"));

        for (String property : propertyNames) {
            assertEquals("Failed assert property: " + property + ", sourceId: " + entity1.get("id") + ", targetId: " + entity2.get("id"),
                    entity1.get(property), entity2.get(property));
        }
    }

    private static <T extends BaseModelData> T entityByName(List<T> entities, String name) {
        for (T entity : entities) {
            if (name.equals(entity.get("name"))) {
                return entity;
            }
        }
        throw new RuntimeException("No entity with name:" + name);
    }

    private static <T extends FormElement> T elementByName(List<T> list, String label) {
        for (T entity : list) {
            if (label.equals(entity.getLabel())) {
                return entity;
            }
        }
        throw new RuntimeException("No FormElement with label:" + label);

    }
}
