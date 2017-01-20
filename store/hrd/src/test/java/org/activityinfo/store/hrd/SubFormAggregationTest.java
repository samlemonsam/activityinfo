package org.activityinfo.store.hrd;


import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.base.Optional;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.service.store.FormStorage;
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
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
                .setDefaultHighRepJobPolicyUnappliedJobPercentage(100));
    private Closeable objectify;

    private int userId = 1;

    @Before
    public void setUp() {
        helper.setUp();
        objectify = ObjectifyService.begin();
    }

    @BeforeClass
    public static void setUpLocale() {
        LocaleProxy.initialize();
    }

    @After
    public void tearDown() {
        objectify.close();
        helper.tearDown();
    }
    
    @Test
    public void subFormAggregationTest() {

        // Typical scenario with a household interview form
        // and a repeating househould member form


        FormClass siteForm = new FormClass(ResourceId.generateId());
        siteForm.setParentFormId(ResourceId.ROOT_ID);

        FormClass monthlyForm = new FormClass(ResourceId.generateId());
        monthlyForm.setParentFormId(siteForm.getId());
        monthlyForm.setSubFormKind(SubFormKind.MONTHLY);

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

        RecordUpdate v1 = new RecordUpdate();
        v1.setUserId(userId);
        v1.setRecordId(ResourceId.generateSubmissionId(siteForm));
        v1.set(villageField.getId(), TextValue.valueOf("Rutshuru"));

        RecordUpdate v2 = new RecordUpdate();
        v2.setUserId(userId);
        v2.setRecordId(ResourceId.generateSubmissionId(siteForm));
        v2.set(villageField.getId(), TextValue.valueOf("Beni"));

        RecordUpdate month1 = new RecordUpdate();
        month1.setUserId(userId);
        month1.setRecordId(ResourceId.generateSubmissionId(monthlyForm));
        month1.setParentId(v1.getRecordId());
        month1.set(countField.getId(), new Quantity(40, "households"));

        RecordUpdate month2 = new RecordUpdate();
        month2.setUserId(userId);
        month2.setRecordId(ResourceId.generateSubmissionId(monthlyForm));
        month2.setParentId(v1.getRecordId());
        month2.set(countField.getId(), new Quantity(30, "households"));

        RecordUpdate month3 = new RecordUpdate();
        month3.setUserId(userId);
        month3.setRecordId(ResourceId.generateSubmissionId(monthlyForm));
        month3.setParentId(v2.getRecordId());
        month3.set(countField.getId(), new Quantity(47, "households"));

        FormStorage siteCollection = catalog.getForm(siteForm.getId()).get();
        siteCollection.add(v1);
        siteCollection.add(v2);

        Optional<FormStorage> monthCollection = catalog.getForm(monthlyForm.getId());
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
