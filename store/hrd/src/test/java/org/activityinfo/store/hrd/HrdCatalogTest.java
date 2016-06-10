package org.activityinfo.store.hrd;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.base.Optional;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceUpdate;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.query.impl.ColumnSetBuilder;
import org.activityinfo.store.query.impl.Updater;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class HrdCatalogTest {
    
    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    @Before
    public void setUp() {
        helper.setUp();
    }
    
    @BeforeClass
    public static void setUpLocale() {
        LocaleProxy.initialize();
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }
    
    @Test
    public void simpleFormTest() {

        ResourceId collectionId = ResourceId.generateId();
        ResourceId villageField = ResourceId.valueOf("FV");
        ResourceId countField = ResourceId.valueOf("FC");
        
        FormClass formClass = new FormClass(collectionId);
        formClass.setOwnerId(ResourceId.valueOf("foo"));
        formClass.setLabel("NFI Distributions");
        formClass.addField(villageField)
                .setLabel("Village name")
                .setCode("VILLAGE")
                .setType(TextType.INSTANCE);
        formClass.addField(countField)
                .setLabel("Number of Beneficiaries")
                .setCode("BENE")
                .setType(new QuantityType("Beneficiaries"));
        
        
        HrdCatalog catalog = new HrdCatalog();
        catalog.create(formClass);

        Optional<ResourceCollection> collection = catalog.getCollection(collectionId);
        
        assertTrue(collection.isPresent());

        ResourceUpdate village1 = new ResourceUpdate();
        village1.setResourceId(ResourceId.generateSubmissionId(formClass));
        village1.set(villageField, TextValue.valueOf("Rutshuru"));
        village1.set(countField, new Quantity(1000));

        ResourceUpdate village2 = new ResourceUpdate();
        village2.setResourceId(ResourceId.generateSubmissionId(formClass));
        village2.set(villageField, TextValue.valueOf("Beni"));
        village2.set(countField, new Quantity(230));

        collection.get().add(village1);
        collection.get().add(village2);

        QueryModel queryModel = new QueryModel(collectionId);
        queryModel.selectResourceId().as("id");
        queryModel.selectField("VILLAGE").as("village");
        queryModel.selectField("BENE").as("family_count");
        queryModel.selectExpr("BENE*5").as("individual_count");
        
        ColumnSetBuilder builder = new ColumnSetBuilder(catalog);
        ColumnSet columnSet = builder.build(queryModel);
        
        System.out.println(columnSet);

        assertThat(columnSet.getNumRows(), equalTo(2));
    }
    
    @Test
    public void createResource() {


        FormClass formClass = new FormClass(ResourceId.generateId());
        formClass.setOwnerId(ResourceId.valueOf("foo"));
        formClass.setLabel("NFI Distributions");
        FormField nameField = formClass.addField(ResourceId.generateId())
                .setLabel("Village name")
                .setCode("VILLAGE")
                .setType(TextType.INSTANCE);

        HrdCatalog catalog = new HrdCatalog();
        Updater updater = new Updater(catalog);

        HrdCollection collection = catalog.create(formClass);
        
        String villageNames[] = new String[] { "Rutshuru" , "Beni", "Goma" };

        for (String villageName : villageNames) {
            ResourceUpdate update = new ResourceUpdate();
            update.setResourceId(ResourceId.generateSubmissionId(formClass));
            update.set(nameField.getId(), TextValue.valueOf(villageName));
        
            updater.execute(update);
        }


        QueryModel queryModel = new QueryModel(formClass.getId());
        queryModel.selectResourceId().as("id");
        queryModel.selectField("VILLAGE").as("village");

        ColumnSetBuilder builder = new ColumnSetBuilder(catalog);
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
        memberForm.setOwnerId(hhForm.getId());

        hhForm.setOwnerId(ResourceId.valueOf("foo"));
        hhForm.setLabel("Household interview");
        FormField hhIdField = hhForm.addField()
                .setLabel("Household ID")
                .setType(TextType.INSTANCE);
        hhForm.addField()
                .setLabel("Household Memmbers")
                .setType(new SubFormReferenceType(memberForm.getId()));


        memberForm.setLabel("Household Members");
        FormField nameField = memberForm.addField()
                .setLabel("Name")
                .setType(TextType.INSTANCE);
        FormField ageField = memberForm.addField()
                .setLabel("Age")
                .setType(new QuantityType("years"));


        HrdCatalog catalog = new HrdCatalog();
        catalog.create(hhForm);
        catalog.create(memberForm);


        ResourceUpdate hh1 = new ResourceUpdate();
        hh1.setResourceId(ResourceId.generateSubmissionId(hhForm));
        hh1.set(hhIdField.getId(), TextValue.valueOf("HH1"));

        ResourceUpdate hh2 = new ResourceUpdate();
        hh2.setResourceId(ResourceId.generateSubmissionId(hhForm));
        hh2.set(hhIdField.getId(), TextValue.valueOf("HH2"));
        
        ResourceUpdate father1 = new ResourceUpdate();
        father1.setResourceId(ResourceId.generateSubmissionId(memberForm));
        father1.setParentId(hh1.getResourceId());
        father1.set(nameField.getId(), TextValue.valueOf("Homer"));
        father1.set(ageField.getId(), new Quantity(40, "years"));
        
        ResourceUpdate father2 = new ResourceUpdate();
        father2.setResourceId(ResourceId.generateSubmissionId(memberForm));
        father2.setParentId(hh2.getResourceId());
        father2.set(nameField.getId(), TextValue.valueOf("Ned"));
        father2.set(ageField.getId(), new Quantity(41, "years"));
        
        Optional<ResourceCollection> hhCollection = catalog.getCollection(hhForm.getId());
        assertTrue(hhCollection.isPresent());
        
        hhCollection.get().add(hh1);
        hhCollection.get().add(hh2);

        Optional<ResourceCollection> memberCollection = catalog.getCollection(memberForm.getId());
        assertTrue(memberCollection.isPresent());
        
        memberCollection.get().add(father1);
        memberCollection.get().add(father2);
        
        QueryModel queryModel = new QueryModel(memberForm.getId());
        queryModel.selectResourceId().as("id");
        queryModel.selectField("Household ID").as("hh_id");
        queryModel.selectField("Name").as("member_name");
        queryModel.selectField("Age").as("member_age");

        ColumnSetBuilder builder = new ColumnSetBuilder(catalog);
        ColumnSet columnSet = builder.build(queryModel);

        System.out.println(columnSet);

        assertThat(columnSet.getNumRows(), equalTo(2));
    }

}
