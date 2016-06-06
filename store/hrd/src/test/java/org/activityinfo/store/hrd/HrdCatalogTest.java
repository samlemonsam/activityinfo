package org.activityinfo.store.hrd;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.base.Optional;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceUpdate;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.query.impl.ColumnSetBuilder;
import org.junit.After;
import org.junit.Before;
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
}
