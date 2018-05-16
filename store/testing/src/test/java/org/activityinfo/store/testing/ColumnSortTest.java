package org.activityinfo.store.testing;

import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.query.SortModel;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.shared.NullFormScanCache;
import org.activityinfo.store.query.shared.NullFormSupervisor;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class ColumnSortTest {

    private TestingStorageProvider testingCatalog;

    @Before
    public void setup() {
        testingCatalog = new TestingStorageProvider();
    }

    @Test
    public void missingQuantityValues() {
        Survey form = testingCatalog.getSurvey();

        QueryModel queryModel = new QueryModel(form.getFormId());
        queryModel.selectField(form.getChildrenFieldId()).as("children");

        queryModel.addSortModel(new SortModel("children", SortModel.Dir.DESC));
        ColumnSet columnSet = query(queryModel);
        ColumnView childColumn = columnSet.getColumnView("children");

        assertThat(columnSet.getNumRows(), equalTo(Survey.getRowCount()));
        assertFalse(childColumn.isMissing(0));
        assertTrue(childColumn.isMissing(Survey.getRowCount()-1));
        assertThat(childColumn.get(0), equalTo(7.0));
        assertThat(childColumn.get(Survey.getRowCount()-1), equalTo(Double.NaN));

        queryModel.getSortModels().clear();
        queryModel.addSortModel(new SortModel("children", SortModel.Dir.ASC));
        columnSet = query(queryModel);
        childColumn = columnSet.getColumnView("children");

        assertThat(columnSet.getNumRows(), equalTo(Survey.getRowCount()));
        assertTrue(childColumn.isMissing(0));
        assertFalse(childColumn.isMissing(Survey.getRowCount()-1));
        assertThat(childColumn.get(0), equalTo(Double.NaN));
        assertThat(childColumn.get(Survey.getRowCount()-1), equalTo(7.0));
    }

    @Test
    public void missingStringValues() {
        Survey form = testingCatalog.getSurvey();

        QueryModel queryModel = new QueryModel(form.getFormId());
        queryModel.selectField(form.getSpouseFieldId()).as("spouse");

        queryModel.addSortModel(new SortModel("spouse", SortModel.Dir.DESC));
        ColumnSet columnSet = query(queryModel);
        ColumnView spouseColumn = columnSet.getColumnView("spouse");

        assertThat(columnSet.getNumRows(), equalTo(Survey.getRowCount()));
        assertFalse(spouseColumn.isMissing(0));
        assertTrue(spouseColumn.isMissing(Survey.getRowCount()-1));
        assertThat(spouseColumn.get(0), equalTo("Sue"));
        assertNull(spouseColumn.get(Survey.getRowCount()-1));

        queryModel.getSortModels().clear();
        queryModel.addSortModel(new SortModel("spouse", SortModel.Dir.ASC));
        columnSet = query(queryModel);
        spouseColumn = columnSet.getColumnView("spouse");

        assertThat(columnSet.getNumRows(), equalTo(Survey.getRowCount()));
        assertTrue(spouseColumn.isMissing(0));
        assertFalse(spouseColumn.isMissing(Survey.getRowCount()-1));
        assertNull(spouseColumn.get(0));
        assertThat(spouseColumn.get(Survey.getRowCount()-1), equalTo("Sue"));
    }

    @Test
    public void missingEnumValues() {
        Survey form = testingCatalog.getSurvey();

        QueryModel queryModel = new QueryModel(form.getFormId());
        queryModel.selectField(form.getPregnantFieldId()).as("pregnant");

        queryModel.addSortModel(new SortModel("pregnant", SortModel.Dir.DESC));
        ColumnSet columnSet = query(queryModel);
        ColumnView pregnantColumn = columnSet.getColumnView("pregnant");

        assertThat(columnSet.getNumRows(), equalTo(Survey.getRowCount()));
        //assertFalse(pregnantColumn.isMissing(0));
        //assertTrue(pregnantColumn.isMissing(Survey.getRowCount()-1));
        //assertThat(pregnantColumn.get(0), equalTo("Sue"));
        //assertNull(pregnantColumn.get(Survey.getRowCount()-1));

        queryModel.getSortModels().clear();
        queryModel.addSortModel(new SortModel("pregnant", SortModel.Dir.ASC));
        columnSet = query(queryModel);
        pregnantColumn = columnSet.getColumnView("pregnant");

        assertThat(columnSet.getNumRows(), equalTo(Survey.getRowCount()));
        //assertTrue(pregnantColumn.isMissing(0));
        //assertFalse(pregnantColumn.isMissing(Survey.getRowCount()-1));
        //assertNull(pregnantColumn.get(0));
        //assertThat(pregnantColumn.get(Survey.getRowCount()-1), equalTo("Sue"));

    }

    private ColumnSet query(QueryModel queryModel) {
        ColumnSetBuilder builder = new ColumnSetBuilder(testingCatalog, new NullFormScanCache(), new NullFormSupervisor());
        return builder.build(queryModel);
    }

}
