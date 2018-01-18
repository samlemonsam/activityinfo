package org.activityinfo.store.testing;

import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.shared.NullFormScanCache;
import org.activityinfo.store.query.shared.NullFormSupervisor;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test queries involving date functions
 */
public class DateFunctionQueryTest {

    @Test
    public void testFloorToday() {
        TestingStorageProvider catalog = new TestingStorageProvider();
        IntakeForm intakeForm = catalog.getIntakeForm();
        ColumnSetBuilder builder = new ColumnSetBuilder(catalog, new NullFormScanCache(), new NullFormSupervisor());

        QueryModel queryModel = new QueryModel(intakeForm.getFormId());
        queryModel.selectExpr("TODAY()").as("today");
        queryModel.selectExpr("FLOOR(YEARFRAC(\"2017-05-05\", DOB))").as("ageFloored");
        queryModel.selectExpr("FLOOR(1.5)").as("floor");
        queryModel.selectExpr("DOB").as("dob");

        ColumnSet columnSet = builder.build(queryModel);
        ColumnView floor = columnSet.getColumnView("floor");
        ColumnView ageFloored = columnSet.getColumnView("ageFloored");

        assertThat(floor.numRows(),equalTo(intakeForm.getRecords().size()));
        assertThat(floor.getDouble(0),equalTo(1.0));
        assertThat(ageFloored.getDouble(0),equalTo(45.0));
        assertThat(ageFloored.getDouble(3),equalTo(29.0));

        System.out.println(ageFloored);

    }

    @Test
    public void testToday() {
        TestingStorageProvider catalog = new TestingStorageProvider();
        IntakeForm intakeForm = catalog.getIntakeForm();
        ColumnSetBuilder builder = new ColumnSetBuilder(catalog, new NullFormScanCache(), new NullFormSupervisor());

        QueryModel queryModel = new QueryModel(intakeForm.getFormId());
        queryModel.selectExpr("TODAY()").as("today");
        queryModel.selectExpr("YEARFRAC(TODAY(), DOB)").as("age");
        queryModel.selectExpr("DOB").as("dob");

        ColumnSet columnSet = builder.build(queryModel);
        ColumnView today = columnSet.getColumnView("today");
        ColumnView age = columnSet.getColumnView("age");
        ColumnView dob = columnSet.getColumnView("dob");

        for (int i = 0; i < columnSet.getNumRows(); i++) {
            if(dob.getString(i) != null) {
                double ageInYears = age.getDouble(i);
                System.out.println(dob.get(i) + " " + ageInYears);
                if (Double.isNaN(ageInYears)) {
                    throw new AssertionError();
                }
            }
        }

        System.out.println(age);
    }


    @Test
    public void invalidArityTest() {
        TestingStorageProvider catalog = new TestingStorageProvider();
        IntakeForm intakeForm = catalog.getIntakeForm();
        ColumnSetBuilder builder = new ColumnSetBuilder(catalog, new NullFormScanCache(), new NullFormSupervisor());

        QueryModel queryModel = new QueryModel(intakeForm.getFormId());
        queryModel.selectExpr("YEARFRAC(TODAY())").as("age");

        ColumnSet columnSet = builder.build(queryModel);
        ColumnView age = columnSet.getColumnView("age");

        assertThat(age.numRows(), equalTo(IntakeForm.ROW_COUNT));

        System.out.println(age);
    }

}
