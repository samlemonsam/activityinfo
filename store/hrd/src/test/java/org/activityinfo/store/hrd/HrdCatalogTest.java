/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.store.hrd;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.base.Optional;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.util.Closeable;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormSyncSet;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.server.Updater;
import org.activityinfo.store.query.shared.NullFormSupervisor;
import org.activityinfo.store.query.shared.plan.QueryPlan;
import org.activityinfo.store.query.shared.plan.QueryPlanBuilder;
import org.activityinfo.store.spi.*;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class HrdCatalogTest {
    
    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
                    .setDefaultHighRepJobPolicyUnappliedJobPercentage(100));
    
    private int userId = 1;
    private Closeable objectifyCloseable;

    @Before
    public void setUp() {
        helper.setUp();
        objectifyCloseable = ObjectifyService.begin();
    }
    
    @BeforeClass
    public static void setUpLocale() {
        LocaleProxy.initialize();
    }

    @After
    public void tearDown() {
        helper.tearDown();
        objectifyCloseable.close();
    }
    
    @Test
    public void simpleFormTest() throws IOException {

        ResourceId collectionId = ResourceId.generateId();
        ResourceId villageField = ResourceId.valueOf("FV");
        ResourceId countField = ResourceId.valueOf("FC");
        ResourceId popTypeField = ResourceId.valueOf("PT");
        
        FormClass formClass = new FormClass(collectionId);
        formClass.setParentFormId(ResourceId.valueOf("foo"));
        formClass.setLabel("NFI Distributions");
        formClass.addField(villageField)
                .setLabel("Village name")
                .setCode("VILLAGE")
                .setType(TextType.SIMPLE);
        formClass.addField(countField)
                .setLabel("Number of Beneficiaries")
                .setCode("BENE")
                .setType(new QuantityType("Beneficiaries"));

        formClass.addField(popTypeField)
                .setLabel("Population type")
                .setCode("POP")
                .setType(new EnumType(Cardinality.SINGLE,
                        new EnumItem(ResourceId.valueOf("POP1"), "Refugees"),
                        new EnumItem(ResourceId.valueOf("POP2"), "IDPs")));

        
        HrdStorageProvider catalog = new HrdStorageProvider();
        catalog.create(formClass);

        Optional<FormStorage> storage = catalog.getForm(collectionId);
        
        assertTrue(storage.isPresent());

        TypedRecordUpdate village1 = new TypedRecordUpdate();
        village1.setUserId(userId);
        village1.setRecordId(ResourceId.generateSubmissionId(formClass));
        village1.set(villageField, TextValue.valueOf("Rutshuru"));
        village1.set(countField, new Quantity(1000));


        TypedRecordUpdate village2 = new TypedRecordUpdate();
        village2.setUserId(userId);
        village2.setRecordId(ResourceId.generateSubmissionId(formClass));
        village2.set(villageField, TextValue.valueOf("Beni"));
        village2.set(countField, new Quantity(230));
        village2.set(popTypeField, new EnumValue(ResourceId.valueOf("POP2")));

        storage.get().add(village1);
        storage.get().add(village2);

        QueryModel queryModel = new QueryModel(collectionId);
        queryModel.selectRecordId().as("id");
        queryModel.selectField("VILLAGE").as("village");
        queryModel.selectField("BENE").as("family_count");
        queryModel.selectExpr("BENE*5").as("individual_count");
        queryModel.selectExpr("POP").as("pop");

        QueryPlanBuilder queryPlanBuilder = new QueryPlanBuilder(catalog);
        QueryPlan plan = queryPlanBuilder.build(queryModel);
        plan.dumpGraph();

        ColumnSetBuilder builder = new ColumnSetBuilder(catalog, new NullFormSupervisor());
        ColumnSet columnSet = builder.build(queryModel);
        
        System.out.println(columnSet);

        assertThat(columnSet.getNumRows(), equalTo(2));
        assertThat(columnSet.getColumnView("id").getString(0), equalTo(village1.getRecordId().asString()));

        assertThat(columnSet.getColumnView("village").getString(0), equalTo("Rutshuru"));
        assertThat(columnSet.getColumnView("village").getString(1), equalTo("Beni"));

        assertThat(columnSet.getColumnView("family_count").getDouble(0), equalTo(1000d));
        assertThat(columnSet.getColumnView("family_count").getDouble(1), equalTo(230d));

        assertThat(columnSet.getColumnView("pop").getString(0), nullValue());
        assertThat(columnSet.getColumnView("pop").getString(1), equalTo("IDPs"));

        List<RecordVersion> versions1 = ((VersionedFormStorage) storage.get()).getVersions(village1.getRecordId());

        assertThat(versions1, hasSize(1));

        RecordVersion version = versions1.get(0);
        assertThat(version.getRecordId(), equalTo(village1.getRecordId()));
        assertThat(version.getUserId(), equalTo((long)userId));
        assertThat(version.getType(), equalTo(RecordChangeType.CREATED));
    }


    @Test
    public void enumWithNoChoices() {

        final ResourceId formId = ResourceId.generateId();
        ResourceId villageField = ResourceId.valueOf("FV");
        final ResourceId selectField = ResourceId.valueOf("FC");

        FormClass formClass = new FormClass(formId);
        formClass.setParentFormId(ResourceId.valueOf("foo"));
        formClass.setLabel("NFI Distributions");
        formClass.addField(villageField)
                .setLabel("Village name")
                .setCode("VILLAGE")
                .setType(TextType.SIMPLE);
        formClass.addField(selectField)
                .setLabel("Favorite color")
                .setType(new EnumType(Cardinality.SINGLE, EnumType.Presentation.AUTOMATIC, Collections.<EnumItem>emptyList()));

        HrdStorageProvider catalog = new HrdStorageProvider();
        catalog.create(formClass);

        // Avoid cache
//        objectifyCloseable.close();

        ObjectifyService.run(new VoidWork() {
            @Override
            public void vrun() {

                HrdStorageProvider catalog = new HrdStorageProvider();
                Optional<FormStorage> storage = catalog.getForm(formId);

                FormClass deserializedSchema = storage.get().getFormClass();

            }
        });
    }



    @Test
    public void createResource() {


        FormClass formClass = new FormClass(ResourceId.generateId());
        formClass.setParentFormId(ResourceId.valueOf("foo"));
        formClass.setLabel("NFI Distributions");
        FormField nameField = formClass.addField(ResourceId.generateId())
                .setLabel("Village name")
                .setCode("VILLAGE")
                .setType(TextType.SIMPLE);

        HrdStorageProvider catalog = new HrdStorageProvider();
        Updater updater = new Updater(catalog, userId, new BlobAuthorizerStub(),
                new HrdSerialNumberProvider());

        catalog.create(formClass);
        
        String villageNames[] = new String[] { "Rutshuru" , "Beni", "Goma" };

        for (String villageName : villageNames) {
            TypedRecordUpdate update = new TypedRecordUpdate();
            update.setUserId(userId);
            update.setFormId(formClass.getId());
            update.setRecordId(ResourceId.generateSubmissionId(formClass));
            update.set(nameField.getId(), TextValue.valueOf(villageName));
        
            updater.execute(update);
        }


        QueryModel queryModel = new QueryModel(formClass.getId());
        queryModel.selectRecordId().as("id");
        queryModel.selectField("VILLAGE").as("village");

        ColumnSetBuilder builder = new ColumnSetBuilder(catalog, new NullFormSupervisor());
        ColumnSet columnSet = builder.build(queryModel);

        
        System.out.println(columnSet);
    }

    @Test
    public void subFormTest() {

        // Typical scenario with a household interview form
        // and a repeating househould member form


        FormClass hhForm = new FormClass(ResourceId.generateId());
        
        FormClass memberForm = new FormClass(ResourceId.generateId());
        memberForm.setParentFormId(hhForm.getId());

        hhForm.setParentFormId(ResourceId.valueOf("foo"));
        hhForm.setLabel("Household interview");
        FormField hhIdField = hhForm.addField()
                .setLabel("Household ID")
                .setType(TextType.SIMPLE);
        hhForm.addField()
                .setLabel("Household Memmbers")
                .setType(new SubFormReferenceType(memberForm.getId()));


        memberForm.setLabel("Household Members");
        FormField nameField = memberForm.addField()
                .setLabel("Name")
                .setType(TextType.SIMPLE);
        FormField ageField = memberForm.addField()
                .setLabel("Age")
                .setType(new QuantityType("years"));


        HrdStorageProvider catalog = new HrdStorageProvider();
        catalog.create(hhForm);
        catalog.create(memberForm);


        TypedRecordUpdate hh1 = new TypedRecordUpdate();
        hh1.setUserId(userId);
        hh1.setRecordId(ResourceId.generateSubmissionId(hhForm));
        hh1.set(hhIdField.getId(), TextValue.valueOf("HH1"));

        TypedRecordUpdate hh2 = new TypedRecordUpdate();
        hh2.setUserId(userId);
        hh2.setRecordId(ResourceId.generateSubmissionId(hhForm));
        hh2.set(hhIdField.getId(), TextValue.valueOf("HH2"));
        
        TypedRecordUpdate father1 = new TypedRecordUpdate();
        father1.setUserId(userId);
        father1.setRecordId(ResourceId.generateSubmissionId(memberForm));
        father1.setParentId(hh1.getRecordId());
        father1.set(nameField.getId(), TextValue.valueOf("Homer"));
        father1.set(ageField.getId(), new Quantity(40));
        
        TypedRecordUpdate father2 = new TypedRecordUpdate();
        father2.setUserId(userId);
        father2.setRecordId(ResourceId.generateSubmissionId(memberForm));
        father2.setParentId(hh2.getRecordId());
        father2.set(nameField.getId(), TextValue.valueOf("Ned"));
        father2.set(ageField.getId(), new Quantity(41));
        
        Optional<FormStorage> hhCollection = catalog.getForm(hhForm.getId());
        assertTrue(hhCollection.isPresent());
        
        hhCollection.get().add(hh1);
        hhCollection.get().add(hh2);

        Optional<FormStorage> memberCollection = catalog.getForm(memberForm.getId());
        assertTrue(memberCollection.isPresent());
        
        memberCollection.get().add(father1);
        memberCollection.get().add(father2);
        
        QueryModel queryModel = new QueryModel(memberForm.getId());
        queryModel.selectRecordId().as("id");
        queryModel.selectField("Household ID").as("hh_id");
        queryModel.selectField("Name").as("member_name");
        queryModel.selectField("Age").as("member_age");

        ColumnSetBuilder builder = new ColumnSetBuilder(catalog, new NullFormSupervisor());
        ColumnSet columnSet = builder.build(queryModel);

        System.out.println(columnSet);

        assertThat(columnSet.getNumRows(), equalTo(2));
    }

    @Test
    public void versionRangeTest() {


        ResourceId collectionId = ResourceId.generateId();
        ResourceId villageField = ResourceId.valueOf("FV");
        ResourceId countField = ResourceId.valueOf("FC");

        FormClass formClass = new FormClass(collectionId);
        formClass.setParentFormId(ResourceId.valueOf("foo"));
        formClass.setLabel("NFI Distributions");
        formClass.addField(villageField)
                .setLabel("Village name")
                .setCode("VILLAGE")
                .setType(TextType.SIMPLE);
        formClass.addField(countField)
                .setLabel("Number of Beneficiaries")
                .setCode("BENE")
                .setType(new QuantityType("Beneficiaries"));


        HrdStorageProvider catalog = new HrdStorageProvider();
        catalog.create(formClass);

        VersionedFormStorage formStorage = (VersionedFormStorage) catalog.getForm(collectionId).get();

        // Initially, with no records added, the form should be at version 1
        // and the version range (0, 1] should be empty.
        assertThat(formStorage.cacheVersion(), equalTo(1L));

        FormSyncSet updatedRecords = formStorage.getVersionRange(0, 1L, resourceId -> true,
                java.util.Optional.empty());

        assertTrue(updatedRecords.isEmpty());

        // Add a new record
        TypedRecordUpdate firstUpdate = new TypedRecordUpdate();
        firstUpdate.setUserId(1);
        firstUpdate.setRecordId(ResourceId.generateId());
        firstUpdate.set(villageField, TextValue.valueOf("Goma"));

        catalog.getForm(collectionId).get().add(firstUpdate);

        // Verify that the version is incremented and the version range
        // (0, 2] includes our new record

        assertThat(formStorage.cacheVersion(), equalTo(2L));

        FormSyncSet updated = formStorage.getVersionRange(0, 2L, resourceId -> true, java.util.Optional.empty());
        assertThat(updated.getUpdatedRecordCount(), equalTo(1));

        // Update the first record and add a new one
        TypedRecordUpdate secondUpdate = new TypedRecordUpdate();
        secondUpdate.setUserId(1);
        secondUpdate.setRecordId(ResourceId.generateId());
        secondUpdate.set(villageField, TextValue.valueOf("Rutshuru"));
        catalog.getForm(collectionId).get().add(firstUpdate);

        // Verify that the version is incremented and the version range
        // (1, 2] includes our new record

        assertThat(formStorage.cacheVersion(), equalTo(3L));

        updated = formStorage.getVersionRange(2L, 3L, resourceId -> true, java.util.Optional.empty());
        assertThat(updated.getUpdatedRecordCount(), equalTo(1));

    }

}
