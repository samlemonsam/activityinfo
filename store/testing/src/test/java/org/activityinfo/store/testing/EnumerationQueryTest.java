package org.activityinfo.store.testing;

import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.EnumColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.shared.NullFormScanCache;
import org.activityinfo.store.query.shared.NullFormSupervisor;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class EnumerationQueryTest {

    private static final String NAT_PAL = "Palestinian";

    @Test
    public void enumRefTests() {
        TestingStorageProvider catalog = new TestingStorageProvider();
        ColumnSetBuilder builder = new ColumnSetBuilder(catalog, new NullFormScanCache(), new NullFormSupervisor());

        IntakeForm intakeForm = catalog.getIntakeForm();

        QueryModel queryModel = new QueryModel(intakeForm.getFormId());
        queryModel.selectField(intakeForm.getNationalityFieldId()).as("nationality");
        queryModel.selectExpr(intakeForm.getNationalityFieldId() + "==" + "\"" + intakeForm.getPalestinianId() + "\"").as("palestinian");
        queryModel.selectExpr( "\"" + intakeForm.getPalestinianId() + "\"" + "==" + intakeForm.getNationalityFieldId()).as("palestinianInverse");

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

    @Test
    public void enumIdTests() {
        TestingStorageProvider catalog = new TestingStorageProvider();
        ColumnSetBuilder builder = new ColumnSetBuilder(catalog, new NullFormScanCache(), new NullFormSupervisor());

        QueryModel queryModel = new QueryModel(EmptyForm.FORM_ID);
        queryModel.selectField(EmptyForm.ENUM_FIELD_ID).as("emptyEnum");
        queryModel.selectField(EmptyForm.POP_ENUM_FIELD_ID).as("populatedEnum");

        ColumnSet columnSet = builder.build(queryModel);
        ColumnView emptyEnum = columnSet.getColumnView("emptyEnum");
        ColumnView populatedEnum = columnSet.getColumnView("populatedEnum");

        //// Standard Enum Id Checks
        // Null check on empty set first
        assertThat(emptyEnum.get(0),equalTo(null));
        assertThat(((EnumColumnView)emptyEnum).getId(0),equalTo(null));
        // Value check on populated set
        assertThat(populatedEnum.get(0).toString(),equalTo("One"));
        assertThat(((EnumColumnView)populatedEnum).getId(0),equalTo(EmptyForm.ENUM_ONE_ID.asString()));

        //// Selected Row Enum Id Checks
        ColumnView emptySelectedRow = emptyEnum.select(new int[]{0});
        ColumnView populatedSelectedRow = populatedEnum.select(new int[]{0});

        // Null check on empty set first
        assertThat(emptySelectedRow.get(0),equalTo(null));
        assertThat(((EnumColumnView)emptySelectedRow).getId(0),equalTo(null));
        // Value check on populated set
        assertThat(populatedSelectedRow.get(0).toString(),equalTo("One"));
        assertThat(((EnumColumnView)populatedSelectedRow).getId(0),equalTo(EmptyForm.ENUM_ONE_ID.asString()));
    }

}