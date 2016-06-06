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
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.query.impl.ColumnSetBuilder;
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
    public void test() {

        ResourceId collectionId = ResourceId.valueOf("XYZ123");
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
        village1.setResourceId(ResourceId.valueOf("V1"));
        village1.set(villageField, TextValue.valueOf("Rutshuru"));
        village1.set(countField, new Quantity(1000));

        ResourceUpdate village2 = new ResourceUpdate();
        village2.setResourceId(ResourceId.valueOf("V2"));
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
    public void subFormTest() {

        // Typical scenario with a household interview form
        // and a repeating househould member form


        FormClass hhForm = new FormClass(ResourceId.generateId());
        
        FormClass memberForm = new FormClass(ResourceId.generateId());
        memberForm.setParentFormId(hhForm.getId());

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
        hh1.setResourceId(ResourceId.generateId());
        hh1.set(hhIdField.getId(), TextValue.valueOf("HH1"));

        ResourceUpdate hh2 = new ResourceUpdate();
        hh2.setResourceId(ResourceId.generateId());
        hh2.set(hhIdField.getId(), TextValue.valueOf("HH2"));
        
        ResourceUpdate father1 = new ResourceUpdate();
        father1.setResourceId(ResourceId.generateId());
        father1.setParentId(hh1.getResourceId());
        father1.set(nameField.getId(), TextValue.valueOf("Homer"));
        father1.set(ageField.getId(), new Quantity(40, "years"));
        
        ResourceUpdate father2 = new ResourceUpdate();
        father2.setResourceId(ResourceId.generateId());
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

    @Test
    public void subFormAggregationTest() {

        // Typical scenario with a household interview form
        // and a repeating househould member form


        FormClass siteForm = new FormClass(ResourceId.generateId());

        FormClass monthlyForm = new FormClass(ResourceId.generateId());
        monthlyForm.setParentFormId(siteForm.getId());

        siteForm.setLabel("Household interview");
        FormField villageField = siteForm.addField()
                .setLabel("Village Name")
                .setType(TextType.INSTANCE);
        siteForm.addField()
                .setLabel("Maximum Beneficiaries")
                .setCode("BENE")
                .setType(new CalculatedFieldType("MAX(HH)"));
        
        siteForm.addField()
                .setLabel("Monthly Activities")
                .setType(new SubFormReferenceType(monthlyForm.getId()));


        monthlyForm.setLabel("Monthly Activities");
        FormField countField = monthlyForm.addField()
                .setLabel("Number of Beneficiaries")
                .setCode("HH")
                .setType(new QuantityType("households"));


        HrdCatalog catalog = new HrdCatalog();
        catalog.create(siteForm);
        catalog.create(monthlyForm);

        ResourceUpdate v1 = new ResourceUpdate();
        v1.setResourceId(ResourceId.generateId());
        v1.set(villageField.getId(), TextValue.valueOf("Rutshuru"));

        ResourceUpdate v2 = new ResourceUpdate();
        v2.setResourceId(ResourceId.generateId());
        v2.set(villageField.getId(), TextValue.valueOf("Beni"));

        ResourceUpdate month1 = new ResourceUpdate();
        month1.setResourceId(ResourceId.generateId());
        month1.setParentId(v1.getResourceId());
        month1.set(countField.getId(), new Quantity(40, "households"));

        ResourceUpdate month2 = new ResourceUpdate();
        month2.setResourceId(ResourceId.generateId());
        month2.setParentId(v1.getResourceId());
        month2.set(countField.getId(), new Quantity(30, "households"));

        ResourceUpdate month3 = new ResourceUpdate();
        month3.setResourceId(ResourceId.generateId());
        month3.setParentId(v1.getResourceId());
        month3.set(countField.getId(), new Quantity(47, "households"));

        ResourceCollection siteCollection = catalog.getCollection(siteForm.getId()).get();
        siteCollection.add(v1);
        siteCollection.add(v2);

        Optional<ResourceCollection> monthCollection = catalog.getCollection(monthlyForm.getId());
        assertTrue(monthCollection.isPresent());

        monthCollection.get().add(month1);
        monthCollection.get().add(month2);
        monthCollection.get().add(month3);

        QueryModel queryModel = new QueryModel(siteForm.getId());
        queryModel.selectResourceId().as("id");
        queryModel.selectField("Village Name").as("village");
        queryModel.selectField("BENE").as("max_hh");

        ColumnSetBuilder builder = new ColumnSetBuilder(catalog);
        ColumnSet columnSet = builder.build(queryModel);

        System.out.println(columnSet);

        assertThat(columnSet.getNumRows(), equalTo(2));
    }
    
}
