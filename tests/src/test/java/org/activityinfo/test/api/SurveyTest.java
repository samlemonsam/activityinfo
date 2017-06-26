package org.activityinfo.test.api;

import org.activityinfo.client.ActivityInfoClient;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.RecordTransactionBuilder;
import org.activityinfo.store.testing.RecordGenerator;
import org.activityinfo.store.testing.Survey;
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
        TestDatabase database = harness.createDatabase();
        client = harness.client();

        survey = new Survey(new SiteIds(database));

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
            client.createRecord(generator.get());
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
