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
package org.activityinfo.test.api;

import org.activityinfo.client.ActivityInfoClient;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.TypedFormRecord;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.RecordTransactionBuilder;
import org.activityinfo.store.testing.RecordGenerator;
import org.activityinfo.store.testing.Survey;
import org.activityinfo.test.driver.ApplicationDriver;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Api Tests against the Survey form...
 */
public class SurveyTest {

    private ApiTestHarness harness = new ApiTestHarness();
    private Survey survey;
    private ActivityInfoClient client;

    @Before
    public void setupForm() {
        ApplicationDriver driver = harness.newUser();
        TestDatabase database = driver.createDatabase();
        client = driver.getClient();

        survey = new Survey(new Cuids(database));

        client.createForm(survey.getFormClass());
    }

    @Test
    public void queryFormClass() {

        // Verify that we read the form class

        FormClass formSchema = client.getFormSchema(survey.getFormId());


    }

    @Test
    public void updateAndQuery() {
        // Post a number of records
        RecordGenerator generator = survey.getGenerator();
        int numRows = 20;
        for (int i = 0; i < numRows; i++) {
            TypedFormRecord newRecord = generator.get();
            System.out.println(newRecord);
            client.createRecord(newRecord);
        }

        // Now query a few records
        QueryModel queryModel = new QueryModel(survey.getFormId());
        queryModel.selectField(survey.getGenderFieldId()).as("gender");
        queryModel.selectField(survey.getAgeFieldId()).as("age");
        queryModel.selectField(survey.getDobFieldId()).as("dob");

        ColumnSet columnSet = client.queryTable(queryModel);
        ColumnView gender = columnSet.getColumnView("gender");
        ColumnView age = columnSet.getColumnView("age");
        ColumnView dob = columnSet.getColumnView("dob");

        assertThat(gender.numRows(), equalTo(numRows));
        assertThat(gender.getType(), equalTo(ColumnType.STRING));

        assertThat(age.numRows(), equalTo(numRows));

    }

    @Test
    public void updateTransaction() {

        RecordTransactionBuilder tx = new RecordTransactionBuilder();
        tx.create(survey.getGenerator().get());
        tx.create(survey.getGenerator().get());

        client.update(tx.build());

        QueryModel queryModel = new QueryModel(survey.getFormId());
        queryModel.selectField(survey.getGenderFieldId()).as("gender");

        ColumnSet columnSet = client.queryTable(queryModel);

        assertThat(columnSet.getNumRows(), equalTo(2));

    }




}
