package org.activityinfo.store.testing;

import org.activityinfo.model.query.*;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.shared.NullFormScanCache;
import org.activityinfo.store.query.shared.NullFormSupervisor;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class ColumnSortTest {

    private TestingStorageProvider testingCatalog;
    private Survey2 form;
    private int numRows;

    @Before
    public void setup() {
        testingCatalog = new TestingStorageProvider();
        form = testingCatalog.getSurvey2();
        numRows = Survey2.getRowCount();
    }

    @Test
    public void missingQuantityValues() {
        QueryModel queryModel = new QueryModel(form.getFormId());
        queryModel.selectField(form.getChildrenFieldId()).as("children");

        queryModel.addSortModel(new SortModel("children", SortDir.DESC));
        ColumnSet columnSet = query(queryModel);
        ColumnView childColumn = columnSet.getColumnView("children");

        assertThat(columnSet.getNumRows(), equalTo(numRows));
        assertFalse(childColumn.isMissing(0));
        assertTrue(childColumn.isMissing(numRows-1));
        assertThat(childColumn.get(0), equalTo(7.0));
        assertThat(childColumn.get(numRows-1), equalTo(Double.NaN));

        queryModel.getSortModels().clear();
        queryModel.addSortModel(new SortModel("children", SortDir.ASC));
        columnSet = query(queryModel);
        childColumn = columnSet.getColumnView("children");

        assertThat(columnSet.getNumRows(), equalTo(numRows));
        assertTrue(childColumn.isMissing(0));
        assertFalse(childColumn.isMissing(numRows-1));
        assertThat(childColumn.get(0), equalTo(Double.NaN));
        assertThat(childColumn.get(numRows-1), equalTo(7.0));
    }

    @Test
    public void missingStringValues() {
        QueryModel queryModel = new QueryModel(form.getFormId());
        queryModel.selectField(form.getSpouseFieldId()).as("spouse");

        queryModel.addSortModel(new SortModel("spouse", SortDir.DESC));
        ColumnSet columnSet = query(queryModel);
        ColumnView spouseColumn = columnSet.getColumnView("spouse");

        assertThat(columnSet.getNumRows(), equalTo(numRows));
        assertFalse(spouseColumn.isMissing(0));
        assertTrue(spouseColumn.isMissing(numRows-1));
        assertThat(spouseColumn.get(0), equalTo("Sue"));
        assertNull(spouseColumn.get(numRows-1));

        queryModel.getSortModels().clear();
        queryModel.addSortModel(new SortModel("spouse", SortDir.ASC));
        columnSet = query(queryModel);
        spouseColumn = columnSet.getColumnView("spouse");

        assertThat(columnSet.getNumRows(), equalTo(numRows));
        assertTrue(spouseColumn.isMissing(0));
        assertFalse(spouseColumn.isMissing(numRows-1));
        assertNull(spouseColumn.get(0));
        assertThat(spouseColumn.get(numRows-1), equalTo("Sue"));
    }

    @Test
    public void missingEnumValues() {
        QueryModel queryModel = new QueryModel(form.getFormId());
        queryModel.selectField(form.getPregnantFieldId()).as("pregnant");

        queryModel.addSortModel(new SortModel("pregnant", SortDir.DESC));
        ColumnSet columnSet = query(queryModel);
        ColumnView pregnantColumn = columnSet.getColumnView("pregnant");

        assertThat(columnSet.getNumRows(), equalTo(numRows));
        assertFalse(pregnantColumn.isMissing(0));
        assertTrue(pregnantColumn.isMissing(numRows-1));
        assertThat(pregnantColumn.get(0), equalTo(form.getPregnantYes().getLabel()));
        assertNull(pregnantColumn.get(numRows-1));

        queryModel.getSortModels().clear();
        queryModel.addSortModel(new SortModel("pregnant", SortDir.ASC));
        columnSet = query(queryModel);
        pregnantColumn = columnSet.getColumnView("pregnant");

        assertThat(columnSet.getNumRows(), equalTo(numRows));
        assertTrue(pregnantColumn.isMissing(0));
        assertFalse(pregnantColumn.isMissing(numRows-1));
        assertNull(pregnantColumn.get(0));
        assertThat(pregnantColumn.get(numRows-1), equalTo(form.getPregnantYes().getLabel()));

    }

    @Test
    public void multiSortTest() {
        QueryModel queryModel = new QueryModel(form.getFormId());
        queryModel.selectField(form.getNameFieldId()).as("name");
        queryModel.selectField(form.getSpouseFieldId()).as("spouse");
        queryModel.selectField(form.getChildrenFieldId()).as("children");
        queryModel.selectField(form.getPregnantFieldId()).as("pregnant");

        ColumnSet columnSet;

        // Unsorted
        columnSet = query(queryModel);
        ColumnView nameColumn = columnSet.getColumnView("name");
        ColumnView spouseColumn = columnSet.getColumnView("spouse");
        ColumnView childrenColumn = columnSet.getColumnView("children");
        ColumnView pregnantColumn = columnSet.getColumnView("pregnant");

        assertThat(columnSet.getNumRows(), equalTo(numRows));
        assertThat(nameColumn.get(0), equalTo("Melanie"));
        assertThat(nameColumn.get(numRows-1), equalTo("Joe"));
        assertThat(spouseColumn.get(0), equalTo("Melanie"));
        assertNull(spouseColumn.get(numRows-1));
        assertThat(childrenColumn.get(0), equalTo(4.0));
        assertThat(childrenColumn.get(numRows-1), equalTo(6.0));
        assertThat(pregnantColumn.get(0), equalTo(form.getPregnantYes().getLabel()));
        assertThat(pregnantColumn.get(numRows-1), equalTo(form.getPregnantYes().getLabel()));

        // Sorted by:
        // - Name, ASC
        queryModel.getSortModels().clear();
        queryModel.addSortModel(new SortModel("name", SortDir.ASC));
        columnSet = query(queryModel);
        checkNameAsc(columnSet);

        // Sorted by:
        // - Name, DESC
        queryModel.getSortModels().clear();
        queryModel.addSortModel(new SortModel("name", SortDir.DESC));
        columnSet = query(queryModel);
        checkNameDesc(columnSet);

        // Sorted by:
        // - Name, DESC
        // - Spouse, ASC
        queryModel.getSortModels().clear();
        queryModel.addSortModel(new SortModel("name", SortDir.DESC));
        queryModel.addSortModel(new SortModel("spouse", SortDir.ASC));
        columnSet = query(queryModel);
        checkNameDesc(columnSet);
        checkNameDescSpouseAsc(columnSet);

        // Sorted by:
        // - Name, DESC
        // - Spouse, DESC
        queryModel.getSortModels().clear();
        queryModel.addSortModel(new SortModel("name", SortDir.DESC));
        queryModel.addSortModel(new SortModel("spouse", SortDir.DESC));
        columnSet = query(queryModel);
        checkNameDesc(columnSet);
        checkNameDescSpouseDesc(columnSet);

        // Sorted by:
        // - Name, DESC
        // - Spouse, DESC
        // - Children, ASC
        queryModel.getSortModels().clear();
        queryModel.addSortModel(new SortModel("name", SortDir.DESC));
        queryModel.addSortModel(new SortModel("spouse", SortDir.DESC));
        queryModel.addSortModel(new SortModel("children", SortDir.ASC));
        columnSet = query(queryModel);
        checkNameDesc(columnSet);
        checkNameDescSpouseDesc(columnSet);
        checkNameDescSpouseDescChildAsc(columnSet);

        // Sorted by:
        // - Name, DESC
        // - Spouse, DESC
        // - Children, DESC
        queryModel.getSortModels().clear();
        queryModel.addSortModel(new SortModel("name", SortDir.DESC));
        queryModel.addSortModel(new SortModel("spouse", SortDir.DESC));
        queryModel.addSortModel(new SortModel("children", SortDir.DESC));
        columnSet = query(queryModel);
        checkNameDesc(columnSet);
        checkNameDescSpouseDesc(columnSet);
        checkNameDescSpouseDescChildDesc(columnSet);

        // Sorted by:
        // - Name, DESC
        // -- Spouse, DESC
        // --- Children, DESC
        // ---- Pregnant, ASC
        queryModel.getSortModels().clear();
        queryModel.addSortModel(new SortModel("name", SortDir.DESC));
        queryModel.addSortModel(new SortModel("spouse", SortDir.DESC));
        queryModel.addSortModel(new SortModel("children", SortDir.DESC));
        queryModel.addSortModel(new SortModel("pregnant", SortDir.ASC));
        columnSet = query(queryModel);
        checkNameDesc(columnSet);
        checkNameDescSpouseDesc(columnSet);
        checkNameDescSpouseDescChildDesc(columnSet);
        checkNameDescSpouseDescChildDescPregnantAsc(columnSet);

        // Sorted by:
        // - Name, DESC
        // -- Spouse, DESC
        // --- Children, DESC
        // ---- Pregnant, DESC
        queryModel.getSortModels().clear();
        queryModel.addSortModel(new SortModel("name", SortDir.DESC));
        queryModel.addSortModel(new SortModel("spouse", SortDir.DESC));
        queryModel.addSortModel(new SortModel("children", SortDir.DESC));
        queryModel.addSortModel(new SortModel("pregnant", SortDir.DESC));
        columnSet = query(queryModel);
        checkNameDesc(columnSet);
        checkNameDescSpouseDesc(columnSet);
        checkNameDescSpouseDescChildDesc(columnSet);
        checkNameDescSpouseDescChildDescPregnantDesc(columnSet);

    }

    private void checkNameAsc(ColumnSet columnSet) {
        assertThat(columnSet.getNumRows(), equalTo(numRows));

        // Sue Group
        assertThat(columnSet.getColumnView("name").get(numRows-1),  equalTo("Sue"));
        assertThat(columnSet.getColumnView("name").get(471),        equalTo("Sue"));

        // Melanie Group
        assertThat(columnSet.getColumnView("name").get(470),        equalTo("Melanie"));
        assertThat(columnSet.getColumnView("name").get(408),        equalTo("Melanie"));

        // Matilda Group
        assertThat(columnSet.getColumnView("name").get(407),        equalTo("Matilda"));
        assertThat(columnSet.getColumnView("name").get(344),        equalTo("Matilda"));

        // Joe Group
        assertThat(columnSet.getColumnView("name").get(343),        equalTo("Joe"));
        assertThat(columnSet.getColumnView("name").get(276),        equalTo("Joe"));

        // Jane Group
        assertThat(columnSet.getColumnView("name").get(275),        equalTo("Jane"));
        assertThat(columnSet.getColumnView("name").get(208),        equalTo("Jane"));

        // George Group
        assertThat(columnSet.getColumnView("name").get(207),        equalTo("George"));
        assertThat(columnSet.getColumnView("name").get(129),        equalTo("George"));

        // Franz Group
        assertThat(columnSet.getColumnView("name").get(128),        equalTo("Franz"));
        assertThat(columnSet.getColumnView("name").get(70),         equalTo("Franz"));

        // Bob Group
        assertThat(columnSet.getColumnView("name").get(69),         equalTo("Bob"));
        assertThat(columnSet.getColumnView("name").get(0),          equalTo("Bob"));
    }

    private void checkNameDesc(ColumnSet columnSet) {
        assertThat(columnSet.getNumRows(), equalTo(numRows));

        // Sue Group
        assertThat(columnSet.getColumnView("name").get(0),          equalTo("Sue"));
        assertThat(columnSet.getColumnView("name").get(64),         equalTo("Sue"));

        // Melanie Group
        assertThat(columnSet.getColumnView("name").get(65),         equalTo("Melanie"));
        assertThat(columnSet.getColumnView("name").get(127),        equalTo("Melanie"));

        // Matilda Group
        assertThat(columnSet.getColumnView("name").get(128),        equalTo("Matilda"));
        assertThat(columnSet.getColumnView("name").get(191),        equalTo("Matilda"));

        // Joe Group
        assertThat(columnSet.getColumnView("name").get(192),        equalTo("Joe"));
        assertThat(columnSet.getColumnView("name").get(259),        equalTo("Joe"));

        // Jane Group
        assertThat(columnSet.getColumnView("name").get(260),        equalTo("Jane"));
        assertThat(columnSet.getColumnView("name").get(327),        equalTo("Jane"));

        // George Group
        assertThat(columnSet.getColumnView("name").get(328),        equalTo("George"));
        assertThat(columnSet.getColumnView("name").get(406),        equalTo("George"));

        // Franz Group
        assertThat(columnSet.getColumnView("name").get(407),        equalTo("Franz"));
        assertThat(columnSet.getColumnView("name").get(465),        equalTo("Franz"));

        // Bob Group
        assertThat(columnSet.getColumnView("name").get(466),        equalTo("Bob"));
        assertThat(columnSet.getColumnView("name").get(numRows-1),  equalTo("Bob"));
    }

    private void checkNameDescSpouseAsc(ColumnSet columnSet) {
        assertThat(columnSet.getNumRows(), equalTo(numRows));

        // Sue Group
        assertThat(columnSet.getColumnView("name").get(0),          equalTo("Sue"));
        assertNull(columnSet.getColumnView("spouse").get(0));
        assertThat(columnSet.getColumnView("name").get(64),         equalTo("Sue"));
        assertThat(columnSet.getColumnView("spouse").get(64),       equalTo("Sue"));

        // Bob Group
        assertThat(columnSet.getColumnView("name").get(466),        equalTo("Bob"));
        assertNull(columnSet.getColumnView("spouse").get(466));
        assertThat(columnSet.getColumnView("name").get(numRows-1),  equalTo("Bob"));
        assertThat(columnSet.getColumnView("spouse").get(numRows-1),equalTo("Sue"));
    }

    private void checkNameDescSpouseDesc(ColumnSet columnSet) {
        assertThat(columnSet.getNumRows(), equalTo(numRows));

        // Sue Group
        assertThat(columnSet.getColumnView("name").get(0),          equalTo("Sue"));
        assertThat(columnSet.getColumnView("spouse").get(0),        equalTo("Sue"));
        assertThat(columnSet.getColumnView("name").get(64),         equalTo("Sue"));
        assertNull(columnSet.getColumnView("spouse").get(64));

        // Bob Group
        assertThat(columnSet.getColumnView("name").get(466),        equalTo("Bob"));
        assertThat(columnSet.getColumnView("spouse").get(466),      equalTo("Sue"));
        assertThat(columnSet.getColumnView("name").get(numRows-1),  equalTo("Bob"));
        assertNull(columnSet.getColumnView("spouse").get(numRows-1));
    }

    private void checkNameDescSpouseDescChildAsc(ColumnSet columnSet) {
        assertThat(columnSet.getNumRows(), equalTo(numRows));

        // Sue -> Sue Group
        assertThat(columnSet.getColumnView("name").get(0),          equalTo("Sue"));
        assertThat(columnSet.getColumnView("spouse").get(0),        equalTo("Sue"));
        assertThat(columnSet.getColumnView("children").get(0),      equalTo(2.0));
        assertThat(columnSet.getColumnView("name").get(5),          equalTo("Sue"));
        assertThat(columnSet.getColumnView("spouse").get(5),        equalTo("Sue"));
        assertThat(columnSet.getColumnView("children").get(5),      equalTo(7.0));

        // Bob -> Sue Group
        assertThat(columnSet.getColumnView("name").get(466),        equalTo("Bob"));
        assertThat(columnSet.getColumnView("spouse").get(466),      equalTo("Sue"));
        assertThat(columnSet.getColumnView("children").get(466),    equalTo(Double.NaN));
        assertThat(columnSet.getColumnView("name").get(474),        equalTo("Bob"));
        assertThat(columnSet.getColumnView("spouse").get(474),      equalTo("Sue"));
        assertThat(columnSet.getColumnView("children").get(474),    equalTo(7.0));
    }

    private void checkNameDescSpouseDescChildDesc(ColumnSet columnSet) {
        assertThat(columnSet.getNumRows(), equalTo(numRows));

        // Sue -> Sue Group
        assertThat(columnSet.getColumnView("name").get(0),          equalTo("Sue"));
        assertThat(columnSet.getColumnView("spouse").get(0),        equalTo("Sue"));
        assertThat(columnSet.getColumnView("children").get(0),      equalTo(7.0));
        assertThat(columnSet.getColumnView("name").get(5),          equalTo("Sue"));
        assertThat(columnSet.getColumnView("spouse").get(5),        equalTo("Sue"));
        assertThat(columnSet.getColumnView("children").get(5),      equalTo(2.0));

        // Bob -> Sue Group
        assertThat(columnSet.getColumnView("name").get(466),        equalTo("Bob"));
        assertThat(columnSet.getColumnView("spouse").get(466),      equalTo("Sue"));
        assertThat(columnSet.getColumnView("children").get(466),    equalTo(7.0));
        assertThat(columnSet.getColumnView("name").get(474),        equalTo("Bob"));
        assertThat(columnSet.getColumnView("spouse").get(474),      equalTo("Sue"));
        assertThat(columnSet.getColumnView("children").get(474),    equalTo(Double.NaN));
    }

    private void checkNameDescSpouseDescChildDescPregnantAsc(ColumnSet columnSet) {
        assertThat(columnSet.getNumRows(), equalTo(numRows));

        // Sue -> Sue -> 7.0 Group
        assertThat(columnSet.getColumnView("name").get(0),          equalTo("Sue"));
        assertThat(columnSet.getColumnView("spouse").get(0),        equalTo("Sue"));
        assertThat(columnSet.getColumnView("children").get(0),      equalTo(7.0));
        assertThat(columnSet.getColumnView("pregnant").get(0),      equalTo(form.getPregnantNo().getLabel()));
        assertThat(columnSet.getColumnView("name").get(1),          equalTo("Sue"));
        assertThat(columnSet.getColumnView("spouse").get(1),        equalTo("Sue"));
        assertThat(columnSet.getColumnView("children").get(1),      equalTo(7.0));
        assertThat(columnSet.getColumnView("pregnant").get(1),      equalTo(form.getPregnantYes().getLabel()));

        // Bob -> Sue -> NaN Group
        assertThat(columnSet.getColumnView("name").get(472),        equalTo("Bob"));
        assertThat(columnSet.getColumnView("spouse").get(472),      equalTo("Sue"));
        assertThat(columnSet.getColumnView("children").get(472),    equalTo(Double.NaN));
        assertThat(columnSet.getColumnView("pregnant").get(472),    equalTo(form.getPregnantNo().getLabel()));
        assertThat(columnSet.getColumnView("name").get(474),        equalTo("Bob"));
        assertThat(columnSet.getColumnView("spouse").get(474),      equalTo("Sue"));
        assertThat(columnSet.getColumnView("children").get(474),    equalTo(Double.NaN));
        assertThat(columnSet.getColumnView("pregnant").get(474),    equalTo(form.getPregnantYes().getLabel()));
    }

    private void checkNameDescSpouseDescChildDescPregnantDesc(ColumnSet columnSet) {
        assertThat(columnSet.getNumRows(), equalTo(numRows));

        // Sue -> Sue -> 7.0 Group
        assertThat(columnSet.getColumnView("name").get(0),          equalTo("Sue"));
        assertThat(columnSet.getColumnView("spouse").get(0),        equalTo("Sue"));
        assertThat(columnSet.getColumnView("children").get(0),      equalTo(7.0));
        assertThat(columnSet.getColumnView("pregnant").get(0),      equalTo(form.getPregnantYes().getLabel()));
        assertThat(columnSet.getColumnView("name").get(1),          equalTo("Sue"));
        assertThat(columnSet.getColumnView("spouse").get(1),        equalTo("Sue"));
        assertThat(columnSet.getColumnView("children").get(1),      equalTo(7.0));
        assertThat(columnSet.getColumnView("pregnant").get(1),      equalTo(form.getPregnantNo().getLabel()));

        // Bob -> Sue -> NaN Group
        assertThat(columnSet.getColumnView("name").get(472),        equalTo("Bob"));
        assertThat(columnSet.getColumnView("spouse").get(472),      equalTo("Sue"));
        assertThat(columnSet.getColumnView("children").get(472),    equalTo(Double.NaN));
        assertThat(columnSet.getColumnView("pregnant").get(472),    equalTo(form.getPregnantYes().getLabel()));
        assertThat(columnSet.getColumnView("name").get(474),        equalTo("Bob"));
        assertThat(columnSet.getColumnView("spouse").get(474),      equalTo("Sue"));
        assertThat(columnSet.getColumnView("children").get(474),    equalTo(Double.NaN));
        assertThat(columnSet.getColumnView("pregnant").get(474),    equalTo(form.getPregnantNo().getLabel()));
    }

    private ColumnSet query(QueryModel queryModel) {
        ColumnSetBuilder builder = new ColumnSetBuilder(testingCatalog, new NullFormScanCache(), new NullFormSupervisor());
        return builder.build(queryModel);
    }

}
