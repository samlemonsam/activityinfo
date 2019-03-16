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
import com.googlecode.objectify.util.Closeable;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.shared.NullFormSupervisor;
import org.activityinfo.store.spi.FormStorage;
import org.activityinfo.store.spi.TypedRecordUpdate;
import org.junit.After;
import org.junit.Before;
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
                .setType(TextType.SIMPLE);
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


        HrdStorageProvider catalog = new HrdStorageProvider();
        catalog.create(siteForm);
        catalog.create(monthlyForm);

        TypedRecordUpdate v1 = new TypedRecordUpdate();
        v1.setUserId(userId);
        v1.setFormId(siteForm.getId());
        v1.setRecordId(ResourceId.generateSubmissionId(siteForm));
        v1.set(villageField.getId(), TextValue.valueOf("Rutshuru"));

        TypedRecordUpdate v2 = new TypedRecordUpdate();
        v2.setUserId(userId);
        v2.setFormId(siteForm.getId());
        v2.setRecordId(ResourceId.generateSubmissionId(siteForm));
        v2.set(villageField.getId(), TextValue.valueOf("Beni"));

        TypedRecordUpdate month1 = new TypedRecordUpdate();
        month1.setUserId(userId);
        month1.setFormId(monthlyForm.getId());
        month1.setRecordId(ResourceId.generateSubmissionId(monthlyForm));
        month1.setParentId(v1.getRecordId());
        month1.set(countField.getId(), new Quantity(40));

        TypedRecordUpdate month2 = new TypedRecordUpdate();
        month2.setUserId(userId);
        month2.setFormId(monthlyForm.getId());
        month2.setRecordId(ResourceId.generateSubmissionId(monthlyForm));
        month2.setParentId(v1.getRecordId());
        month2.set(countField.getId(), new Quantity(30));

        TypedRecordUpdate month3 = new TypedRecordUpdate();
        month3.setUserId(userId);
        month3.setRecordId(ResourceId.generateSubmissionId(monthlyForm));
        month3.setParentId(v2.getRecordId());
        month3.set(countField.getId(), new Quantity(47));

        FormStorage siteCollection = catalog.getForm(siteForm.getId()).get();
        siteCollection.add(v1);
        siteCollection.add(v2);

        Optional<FormStorage> monthCollection = catalog.getForm(monthlyForm.getId());
        assertTrue(monthCollection.isPresent());

        monthCollection.get().add(month1);
        monthCollection.get().add(month2);
        monthCollection.get().add(month3);

        monthCollection = catalog.getForm(monthlyForm.getId());
        assertThat(monthCollection.get().cacheVersion(), equalTo(4L));


        QueryModel queryModel = new QueryModel(siteForm.getId());
        queryModel.selectRecordId().as("id");
        queryModel.selectField("Village Name").as("village");
        queryModel.selectField("BENE").as("max_hh");

        ColumnSetBuilder builder = new ColumnSetBuilder(catalog, new NullFormSupervisor());
        ColumnSet columnSet = builder.build(queryModel);

        System.out.println(columnSet);

        assertThat(columnSet.getNumRows(), equalTo(2));
        assertThat(columnSet.getColumnView("max_hh").getDouble(0), equalTo(40d));
        assertThat(columnSet.getColumnView("max_hh").getDouble(1), equalTo(47d));

    }

}
