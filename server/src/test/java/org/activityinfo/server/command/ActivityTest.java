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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.shared.command.*;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.model.form.*;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.geo.Extents;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.server.database.OnDataSet;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static org.activityinfo.model.legacy.CuidAdapter.activityFormClass;
import static org.activityinfo.promise.PromiseMatchers.assertResolves;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/schema1.db.xml")
public class ActivityTest extends CommandTestCase2 {
    

    @Before
    public void setUser() {
        setUser(1);
    }

    @Test
    public void testActivity() throws CommandException {

        /*
         * Initial data load
         */

        SchemaDTO schema = execute(new GetSchema());

        UserDatabaseDTO db = schema.getDatabaseById(1);

        /*
         * Create a new activity
         */

        LocationTypeDTO locType = schema.getCountryById(1).getLocationTypes().get(0);

        ActivityFormDTO act = new ActivityFormDTO();
        act.setName("Warshing the dishes");
        act.setLocationType(locType);
        act.setReportingFrequency(ActivityFormDTO.REPORT_MONTHLY);
        act.setClassicView(false);

        CreateResult cresult = execute(CreateEntity.Activity(db, act));

        int newId = cresult.getNewId();

        /*
         * Reload schema to verify the changes have stuck
         */


        act = execute(new GetActivityForm(newId));

        assertEquals("name", "Warshing the dishes", act.getName());
        assertEquals("locationType", locType.getName(), act.getLocationType().getName());
        assertEquals("reportingFrequency", ActivityFormDTO.REPORT_MONTHLY, act.getReportingFrequency());
        assertEquals("public", Published.NOT_PUBLISHED.getIndex(), act.getPublished());
        assertEquals("classicView", false, act.getClassicView());

        Extents countryBounds = act.getLocationType().getCountryBounds();
        assertThat(countryBounds.getMinLat(), Matchers.equalTo(-13.0));
        assertThat(countryBounds.getMaxLat(), Matchers.equalTo(5.0));

        assertThat(countryBounds.getMinLon(), Matchers.equalTo(12.0));
        assertThat(countryBounds.getMaxLon(), Matchers.equalTo(31.0));
    }


    @Test
    @OnDataSet("/dbunit/schema-lt-type.db.xml")
    public void testActivityFormWithAdminLocationType() throws CommandException {


        ActivityFormDTO form = execute(new GetActivityForm(1));

        assertEquals("name", "NFI", form.getName());
        assertThat("locationType", form.getLocationType().isAdminLevel(), equalTo(true));

    }

    @Test
    public void updateSortOrderTest() throws Throwable {

        /* Update Sort Order */
        Map<String, Object> changes1 = Maps.newHashMap();
        changes1.put("sortOrder", 2);
        Map<String, Object> changes2 = Maps.newHashMap();
        changes2.put("sortOrder", 1);

        execute(new BatchCommand(
                new UpdateEntity("Activity", 1, changes1),
                new UpdateEntity("Activity", 2, changes2)));

        /* Confirm the order is changed */

        SchemaDTO schema = execute(new GetSchema());
        assertEquals(2, schema.getDatabaseById(1).getActivities().get(0).getId());
        assertEquals(1, schema.getDatabaseById(1).getActivities().get(1).getId());
    }

    @Test
    public void updatePublished() throws Throwable {

        /* Update Sort Order */
        Map<String, Object> changes = Maps.newHashMap();
        changes.put("published", Published.ALL_ARE_PUBLISHED.getIndex());

        execute(new UpdateEntity("Activity", 1, changes));

        /* Confirm the order is changed */

        SchemaDTO schema = execute(new GetSchema());
        assertEquals(Published.ALL_ARE_PUBLISHED.getIndex(), schema.getActivityById(1).getPublished());
    }

    @Test
    public void orderIndicatorsActivities() {

        SchemaDTO schema = execute(new GetSchema());
        UserDatabaseDTO db = schema.getDatabaseById(1);
        LocationTypeDTO locType = schema.getCountryById(1).getLocationTypes().get(0);

        ActivityFormDTO act = new ActivityFormDTO();
        act.setName("Household Survey");
        act.setLocationType(locType);
        act.setReportingFrequency(ActivityFormDTO.REPORT_ONCE);

        CreateResult createResult = execute(CreateEntity.Activity(db, act));
        ResourceId classId = activityFormClass(createResult.getNewId());

        FormClass formClass = assertResolves(locator.getFormClass(classId));

        // create three new fields with an order that mixes "attributes" and "indicators"

        FormField newField = new FormField(ResourceId.generateFieldId(QuantityType.TYPE_CLASS));
        newField.setLabel("How old are you?");
        newField.setType(new QuantityType().setUnits("years"));
        formClass.addElement(newField);

        FormField newGenderField = new FormField(ResourceId.generateFieldId(EnumType.TYPE_CLASS));
        newGenderField.setLabel("Gender");
        EnumItem male = new EnumItem(EnumItem.generateId(), "Male");
        EnumItem female = new EnumItem(EnumItem.generateId(), "Female");
        newGenderField.setType(new EnumType(Cardinality.SINGLE, Arrays.asList(male, female)));
        formClass.addElement(newGenderField);

        FormField newTextField = new FormField(ResourceId.generateFieldId(TextType.TYPE_CLASS));
        newTextField.setLabel("What is your name?");
        newTextField.setType(TextType.SIMPLE);
        formClass.addElement(newTextField);

        assertResolves(locator.persist(formClass));

        TFormClass reform = new TFormClass(assertResolves(locator.getFormClass(formClass.getId())));

        System.out.println(Joiner.on("\n").join(reform.getFormClass().getFields()));

        int a = reform.indexOfField("How old are you?");
        int b = reform.indexOfField("Gender");
        int c = reform.indexOfField("What is your name?");

        assertTrue(a < b && b < c);

    }

    @Test
    public void createActivity() {

        SchemaDTO schema = execute(new GetSchema());
        UserDatabaseDTO db = schema.getDatabaseById(1);
        LocationTypeDTO locType = schema.getCountryById(1).getLocationTypes().get(0);

        ActivityFormDTO act = new ActivityFormDTO();
        act.setName("Household Survey");
        act.setLocationType(locType);
        act.setReportingFrequency(ActivityFormDTO.REPORT_ONCE);

        CreateResult createResult = execute(CreateEntity.Activity(db, act));
        ResourceId classId = activityFormClass(createResult.getNewId());

        FormClass formClass = assertResolves(locator.getFormClass(classId));

        FormField newField = new FormField(ResourceId.generateFieldId(QuantityType.TYPE_CLASS));
        newField.setLabel("How old are you?");
        newField.setType(new QuantityType().setUnits("years"));
        formClass.addElement(newField);

        FormField newTextField = new FormField(ResourceId.generateFieldId(TextType.TYPE_CLASS));
        newTextField.setLabel("What is your name?");
        newTextField.setType(TextType.SIMPLE);
        formClass.addElement(newTextField);

        assertResolves(locator.persist(formClass));
        FormClass reform = assertResolves(locator.getFormClass(formClass.getId()));
        assertHasFieldWithLabel(reform, "How old are you?");

        newField.setLabel("How old are you today?");
        // save again
        assertResolves(locator.persist(formClass));

        reform = assertResolves(locator.getFormClass(formClass.getId()));
        assertHasFieldWithLabel(reform, "How old are you today?");
        System.out.println(reform.getFields().toString());
        assertThat(reform.getFields(), hasSize(8));

        List<EnumItem> values = Lists.newArrayList();
        values.add(new EnumItem(EnumItem.generateId(), "Option 1"));
        values.add(new EnumItem(EnumItem.generateId(), "Option 2"));
    }

    @Test
    public void createAttributeGroup() {

        FormClass formClass = assertResolves(locator.getFormClass(CuidAdapter.activityFormClass(1)));

        FormField newField = new FormField(ResourceId.generateFieldId(EnumType.TYPE_CLASS));
        newField.setLabel("New Group");
        EnumItem yes = new EnumItem(EnumItem.generateId(), "Yes");
        EnumItem no = new EnumItem(EnumItem.generateId(), "No");
        newField.setType(new EnumType(Cardinality.SINGLE, Arrays.asList(yes, no)));

        formClass.getElements().add(newField);

        locator.persist(formClass);

        // verify that it appears as attribute group
        ActivityFormDTO activity = getActivity(1);
        AttributeGroupDTO group = findGroup(activity, "New Group");
        assertThat(group.isMultipleAllowed(), equalTo(false));
        assertThat(group.getAttributes(), hasSize(2));
        assertThat(group.getAttributes().get(0), hasProperty("name", Matchers.equalTo("Yes")));
        assertThat(group.getAttributes().get(1), hasProperty("name", Matchers.equalTo("No")));

        // Now update the same attribute group and a value
        newField.setLabel("Do you like ice cream?");
        yes.setLabel("Oui");
        no.setLabel("Non");
        locator.persist(formClass);

        group = findGroup(getActivity(1), "Do you like ice cream?");
        assertThat(group.isMultipleAllowed(), equalTo(false));
        assertThat(group.getAttributes(), contains(
                hasProperty("name", Matchers.equalTo("Oui")),
                hasProperty("name", Matchers.equalTo("Non"))));

        // Remove one of our new enum values
        newField.setType(new EnumType(Cardinality.SINGLE, Arrays.asList(yes)));
        locator.persist(formClass);

        group = findGroup(getActivity(1), "Do you like ice cream?");
        assertThat(group.isMultipleAllowed(), equalTo(false));
        assertThat(group.getAttributes(), contains(hasProperty("name", Matchers.equalTo("Oui"))));
    }

    @Test
    public void updateIndicator() {

        TFormClass formClass = new TFormClass(assertResolves(locator.getFormClass(CuidAdapter.activityFormClass(1))));

        FormField beneficiaries = formClass.getFieldByLabel("beneficiaries");
        beneficiaries.setLabel("Number of benes");
        locator.persist(formClass.getFormClass());

        ActivityFormDTO activity = getActivity(1);
        assertThat(activity.getIndicatorById(1), hasProperty("name", Matchers.equalTo("Number of benes")));
    }


    @Test
    public void updateIndicatorWithLongUnits() {

        TFormClass formClass = new TFormClass(assertResolves(locator.getFormClass(CuidAdapter.activityFormClass(1))));

        FormField beneficiaries = formClass.getFieldByLabel("beneficiaries");
        QuantityType updatedType = new QuantityType().setUnits("imperial tonne with very long qualifying text");
        beneficiaries.setType(updatedType);
        assertResolves(locator.persist(formClass.getFormClass()));

        ActivityFormDTO activity = getActivity(1);
        assertThat(activity.getIndicatorById(1), hasProperty("units", Matchers.equalTo(updatedType.getUnits())));
    }

   
    private ActivityFormDTO getActivity(int activityId) {
        return execute(new GetActivityForm(activityId));
    }

    private AttributeGroupDTO findGroup(ActivityFormDTO activityDTO, String label) {
        for(AttributeGroupDTO group : activityDTO.getAttributeGroups()) {
            if(group.getName().equals(label)) {
                return group;
            }
        }
        throw new AssertionError("No such attribute group: " + label);
    }


    @Test
    public void deleteAttributeGroup() {

        FormClass formClass = assertResolves(locator.getFormClass(CuidAdapter.activityFormClass(1)));

        // Remove attribute
        ListIterator<FormElement> it = formClass.getElements().listIterator();
        while(it.hasNext()) {
            FormElement element = it.next();
            if(element.getLabel().equals("Cause")) {
                it.remove();
            }
        }
        assertResolves(locator.persist(formClass));

        // Ensure deleted
        ActivityFormDTO form = execute(new GetActivityForm(1));
        assertTrue("Cause attribute is gone", form.getAttributeGroupById(1) == null);

    }
    
    @Test
    public void newFormWithoutDates() {
        
        // Create an activity in the same way that the Design Presenter does...

        int databaseId = 1;

        ActivityDTO newEntity = new ActivityDTO();
        newEntity.setName("My Form");
        newEntity.set("databaseId", databaseId);
        newEntity.set("classicView", false);
        newEntity.set("reportingFrequency", ActivityFormDTO.REPORT_ONCE);
        newEntity.set("locationTypeId", 2 /* Nullary location type for RDC in schema.db.xml */);
        newEntity.set("published", 0);
        CreateResult createResult = execute(new CreateEntity(newEntity));
        
        // Some definitions...
        int activityId = createResult.getNewId();
        ResourceId formId = CuidAdapter.activityFormClass(activityId);
        ResourceId startDateId = CuidAdapter.field(formId, CuidAdapter.START_DATE_FIELD);
        ResourceId endDateId = CuidAdapter.field(formId, CuidAdapter.END_DATE_FIELD);
        
        // Now delete the date fields in the form designer...

        FormClass formClass = assertResolves(locator.getFormClass(formId));
        formClass.removeField(startDateId);
        formClass.removeField(endDateId);
        assertResolves(locator.persist(formClass));

        // Now verify that the form class no longer has the date fields
        formClass = assertResolves(locator.getFormClass(formId));
        assertThat(formClass.getFields(), not(hasItem(withId(startDateId))));
        assertThat(formClass.getFields(), not(hasItem(withId(endDateId))));
        
        // Now submit a new entry without dates...
        FormInstance newInstance = new FormInstance(ResourceId.generateSubmissionId(formId), formId);
        newInstance.set(CuidAdapter.partnerField(activityId), CuidAdapter.partnerRef(databaseId, 1));

        assertResolves(locator.persist(newInstance));
    }

    private static void assertHasFieldWithLabel(FormClass formClass, String label) {
        for (FormField field : formClass.getFields()) {
            if (label.equals(field.getLabel())) {
                return;
            }
        }
        throw new RuntimeException("No field with label: " + label);
    }
    
    private static Matcher<FormField> withId(final ResourceId id) {
        return new TypeSafeMatcher<FormField>() {
            @Override
            protected boolean matchesSafely(FormField formField) {
                return formField.getId().equals(id);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("field with id ");
                description.appendValue(id.asString());
            }
        };
    }
}
