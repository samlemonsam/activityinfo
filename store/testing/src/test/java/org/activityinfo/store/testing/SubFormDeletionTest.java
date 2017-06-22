package org.activityinfo.store.testing;

import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.expr.ConstantExpr;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.TransactionBuilder;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.shared.NullFormScanCache;
import org.activityinfo.store.query.shared.NullFormSupervisor;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class SubFormDeletionTest {

    private TestingCatalog testingCatalog;

    @Before
    public void setup() {
        testingCatalog = new TestingCatalog();
    }


    @Test
    public void deleteParent() {

        QueryModel queryModel = new QueryModel(ReferralSubForm.FORM_ID);
        queryModel.selectResourceId().as("id");
        queryModel.selectExpr(new CompoundExpr(IncidentForm.FORM_ID, ColumnModel.ID_SYMBOL)).as("parent");
        queryModel.selectExpr(new ConstantExpr(1)).as("count");

        ColumnSet columnSet = query(queryModel);
        assertThat(columnSet.getNumRows(), equalTo(ReferralSubForm.ROW_COUNT));
        System.out.println(columnSet.getColumnView("parent"));


        // Now delete a number of parent records
        delete(IncidentForm.FORM_ID, "c528");

        // The query results should reflect the change
        columnSet = query(queryModel);

        assertThat(columnSet.getNumRows(), equalTo(ReferralSubForm.ROW_COUNT - 2));

    }

    @Test
    public void parentQueries() {
        QueryModel queryModel = new QueryModel(ReferralSubForm.FORM_ID);
        queryModel.selectResourceId().as("id");
        queryModel.selectExpr("URGENCY").as("Urgency");

        ColumnSet columnSet = query(queryModel);

    }

    private ColumnSet query(QueryModel queryModel) {
        ColumnSetBuilder builder = new ColumnSetBuilder(testingCatalog, new NullFormScanCache(), new NullFormSupervisor());
        return builder.build(queryModel);
    }


    private void delete(ResourceId formId, String recordId) {

        TransactionBuilder tx = new TransactionBuilder();
        tx.delete(formId, ResourceId.valueOf(recordId));

        testingCatalog.updateRecords(tx);
    }

}
