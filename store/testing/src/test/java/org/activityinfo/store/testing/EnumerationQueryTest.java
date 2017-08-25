package org.activityinfo.store.testing;

import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.store.query.impl.ColumnSetBuilder;
import org.activityinfo.store.query.impl.NullFormScanCache;
import org.activityinfo.store.query.impl.NullFormSupervisor;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class EnumerationQueryTest {

    private static final String NAT_PAL = "Palestinian";

    @Test
    public void enumRefTests() {
        TestingCatalog catalog = new TestingCatalog();
        ColumnSetBuilder builder = new ColumnSetBuilder(catalog, new NullFormScanCache(), new NullFormSupervisor());

        QueryModel queryModel = new QueryModel(IntakeForm.FORM_ID);
        queryModel.selectField(IntakeForm.NATIONALITY_FIELD_ID).as("nationality");
        queryModel.selectExpr(IntakeForm.NATIONALITY_FIELD_ID + "==" + "\"" + IntakeForm.PALESTINIAN_ID + "\"").as("palestinian");
        queryModel.selectExpr( "\"" + IntakeForm.PALESTINIAN_ID + "\"" + "==" + IntakeForm.NATIONALITY_FIELD_ID).as("palestinianInverse");

        ColumnSet columnSet = builder.build(queryModel);
        ColumnView nationality = columnSet.getColumnView("nationality");
        ColumnView palestinian = columnSet.getColumnView("palestinian");
        ColumnView palestinianInverse = columnSet.getColumnView("palestinianInverse");

        // Correct Tests
        assertThat(nationality.get(1).toString(), equalTo(NAT_PAL));
        assertThat(Boolean.valueOf(palestinian.get(1).toString()),equalTo(true));
        assertThat(Boolean.valueOf(palestinianInverse.get(1).toString()),equalTo(true));

        // Incorrect Tests
        assertThat(nationality.get(5), equalTo(null));        // Multiple selected values should return null
    }

}