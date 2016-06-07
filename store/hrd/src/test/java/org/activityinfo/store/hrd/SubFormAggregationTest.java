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

public class SubFormAggregationTest {


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
    public void subFormAggregationTest() {

        // Typical scenario with a household interview form
        // and a repeating househould member form


        FormClass siteForm = new FormClass(ResourceId.valueOf("SITE"));

        FormClass monthlyForm = new FormClass(ResourceId.valueOf("MONTHLY"));
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
        v1.setResourceId(ResourceId.valueOf("V1"));
        v1.set(villageField.getId(), TextValue.valueOf("Rutshuru"));

        ResourceUpdate v2 = new ResourceUpdate();
        v2.setResourceId(ResourceId.valueOf("V2"));
        v2.set(villageField.getId(), TextValue.valueOf("Beni"));

        ResourceUpdate month1 = new ResourceUpdate();
        month1.setResourceId(ResourceId.valueOf("M1"));
        month1.setParentId(v1.getResourceId());
        month1.set(countField.getId(), new Quantity(40, "households"));

        ResourceUpdate month2 = new ResourceUpdate();
        month2.setResourceId(ResourceId.valueOf("M2"));
        month2.setParentId(v1.getResourceId());
        month2.set(countField.getId(), new Quantity(30, "households"));

        ResourceUpdate month3 = new ResourceUpdate();
        month3.setResourceId(ResourceId.valueOf("M3"));
        month3.setParentId(v2.getResourceId());
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
