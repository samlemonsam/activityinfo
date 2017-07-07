package org.activityinfo.store.hrd;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
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
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.server.Updater;
import org.activityinfo.store.query.shared.NullFormSupervisor;
import org.activityinfo.store.spi.*;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
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
    public void simpleFormTest() {

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
        
        
        HrdCatalog catalog = new HrdCatalog();
        catalog.create(formClass);

        Optional<FormStorage> collection = catalog.getForm(collectionId);
        
        assertTrue(collection.isPresent());

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

        collection.get().add(village1);
        collection.get().add(village2);

        QueryModel queryModel = new QueryModel(collectionId);
        queryModel.selectResourceId().as("id");
        queryModel.selectField("VILLAGE").as("village");
        queryModel.selectField("BENE").as("family_count");
        queryModel.selectExpr("BENE*5").as("individual_count");
        
        ColumnSetBuilder builder = new ColumnSetBuilder(catalog, new NullFormSupervisor());
        ColumnSet columnSet = builder.build(queryModel);
        
        System.out.println(columnSet);

        assertThat(columnSet.getNumRows(), equalTo(2));

        List<RecordVersion> versions1 = collection.get().getVersions(village1.getRecordId());
        
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

        HrdCatalog catalog = new HrdCatalog();
        catalog.create(formClass);

        // Avoid cache
//        objectifyCloseable.close();

        ObjectifyService.run(new VoidWork() {
            @Override
            public void vrun() {

                HrdCatalog catalog = new HrdCatalog();
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

        HrdCatalog catalog = new HrdCatalog();
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
        queryModel.selectResourceId().as("id");
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


        HrdCatalog catalog = new HrdCatalog();
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
        father1.set(ageField.getId(), new Quantity(40, "years"));
        
        TypedRecordUpdate father2 = new TypedRecordUpdate();
        father2.setUserId(userId);
        father2.setRecordId(ResourceId.generateSubmissionId(memberForm));
        father2.setParentId(hh2.getRecordId());
        father2.set(nameField.getId(), TextValue.valueOf("Ned"));
        father2.set(ageField.getId(), new Quantity(41, "years"));
        
        Optional<FormStorage> hhCollection = catalog.getForm(hhForm.getId());
        assertTrue(hhCollection.isPresent());
        
        hhCollection.get().add(hh1);
        hhCollection.get().add(hh2);

        Optional<FormStorage> memberCollection = catalog.getForm(memberForm.getId());
        assertTrue(memberCollection.isPresent());
        
        memberCollection.get().add(father1);
        memberCollection.get().add(father2);
        
        QueryModel queryModel = new QueryModel(memberForm.getId());
        queryModel.selectResourceId().as("id");
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


        HrdCatalog catalog = new HrdCatalog();
        catalog.create(formClass);

        VersionedFormStorage formStorage = (VersionedFormStorage) catalog.getForm(collectionId).get();

        // Initially, with no records added, the form should be at version 1
        // and the version range (0, 1] should be empty.
        assertThat(formStorage.cacheVersion(), equalTo(1L));

        FormSyncSet updatedRecords = formStorage.getVersionRange(0, 1L, Predicates.<ResourceId>alwaysTrue());

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

        FormSyncSet updated = formStorage.getVersionRange(0, 2L, Predicates.<ResourceId>alwaysTrue());
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

        updated = formStorage.getVersionRange(2L, 3L, Predicates.<ResourceId>alwaysTrue());
        assertThat(updated.getUpdatedRecordCount(), equalTo(1));

    }

}
