package org.activityinfo.test.driver;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import cucumber.api.DataTable;
import cucumber.runtime.java.guice.ScenarioScoped;
import gherkin.formatter.model.DataTableRow;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.driver.model.IndicatorLink;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.api.XPathBuilder;
import org.activityinfo.test.pageobject.bootstrap.BsFormPanel;
import org.activityinfo.test.pageobject.bootstrap.BsModal;
import org.activityinfo.test.pageobject.gxt.GxtGrid;
import org.activityinfo.test.pageobject.gxt.GxtModal;
import org.activityinfo.test.pageobject.gxt.GxtTree;
import org.activityinfo.test.pageobject.web.ApplicationPage;
import org.activityinfo.test.pageobject.web.LoginPage;
import org.activityinfo.test.pageobject.web.components.Form;
import org.activityinfo.test.pageobject.web.design.*;
import org.activityinfo.test.pageobject.web.design.designer.*;
import org.activityinfo.test.pageobject.web.entry.*;
import org.activityinfo.test.pageobject.web.reports.DrillDownDialog;
import org.activityinfo.test.pageobject.web.reports.PivotTableEditor;
import org.activityinfo.test.sut.UserAccount;
import org.joda.time.LocalDate;
import org.junit.Assert;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.FluentWait;

import javax.inject.Inject;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;


@ScenarioScoped
public class UiApplicationDriver extends ApplicationDriver {

    private ApiApplicationDriver apiDriver;
    private LoginPage loginPage;

    private AliasTable aliasTable;

    private UserAccount currentUser = null;

    private ApplicationPage applicationPage;
    private Object currentPage;
    
    private String currentDatabase;
    private String currentForm;
    
    @Inject
    public UiApplicationDriver(ApiApplicationDriver apiDriver,
                               LoginPage loginPage,
                               AliasTable aliasTable) {
        super(aliasTable);
        this.apiDriver = apiDriver;
        this.loginPage = loginPage;
        this.aliasTable = aliasTable;
    }

    @Override
    public void login() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void login(UserAccount account) {
        if (currentUser == null || !currentUser.getEmail().equals(account.getEmail())) {
            currentUser = account;
            applicationPage = null;

            // defer actually logging in until the first command so that we can
            // let the setup steps execute first
            this.currentUser = account;

            setup().login(account);
        }
    }

    public void ensureLoggedIn() {
        if (applicationPage == null) {
            applicationPage = loginPage.navigateTo().loginAs(currentUser).andExpectSuccess();
            applicationPage.waitUntilLoaded();
        }
    }

    @Override
    public ApplicationDriver setup() {
        return apiDriver;
    }


    // todo ask Alex?
//    @Override
//    public void submitForm(String formName, List<FieldValue> values) throws Exception {
//        currentForm = formName;
//        super.submitForm(formName, values);
//    }
    
    @Override
    public void submitForm(String formName, List<FieldValue> values) throws Exception {
        ensureLoggedIn();

        currentForm = formName;

        Map<String, FieldValue> valueMap = FieldValue.toMap(values);

        DataEntryTab dataEntryTab = applicationPage.navigateToDataEntryTab();
        dataEntryTab.navigateToForm(aliasTable.getAlias(formName));

        DataEntryDriver driver = dataEntryTab.newSubmission();

        fillForm(valueMap, driver);

        driver.submit();
    }

    @Override
    protected DataEntryDriver startNewSubmission(String formName) {
        ensureLoggedIn();

        DataEntryTab dataEntryTab = applicationPage.navigateToDataEntryTab();
        dataEntryTab.navigateToForm(aliasTable.getAlias(formName));

        DataEntryDriver driver = dataEntryTab.newSubmission();

        return driver;
    }

    private void fillForm(Map<String, FieldValue> valueMap, DataEntryDriver driver) throws InterruptedException {
        while (driver.nextField()) {
            System.out.println("label = " + driver.getLabel());
            switch (driver.getLabel()) {
                case "Partner":
                    if (valueMap.containsKey("partner")) {
                        driver.select(aliasTable.getAlias(valueMap.get("partner").getValue()));
                    }
                    break;
                case "Start Date":
                    if (valueMap.containsKey("Start Date")) {
                        driver.fill(LocalDate.parse(valueMap.get("Start Date").getValue()));
                    } else {
                        driver.fill(new LocalDate(2014, 1, 1));
                    }
                    break;
                case "End Date":
                    if (valueMap.containsKey("End Date")) {
                        driver.fill(LocalDate.parse(valueMap.get("End Date").getValue()));
                    } else {
                        driver.fill(new LocalDate(2014, 1, 1));
                    }
                    break;
                case "Comments":
                    if (valueMap.containsKey("comments")) {
                        driver.fill(valueMap.get("comments").getValue());
                    }
                    break;
                default:
                    String testHandle = aliasTable.getTestHandleForAlias(driver.getLabel());
                    if (valueMap.containsKey(testHandle)) {
                        String value = valueMap.get(testHandle).getValue();
                        if (value.matches("^\\d+$")) {
                            driver.fill(value);
                        } else {
                            driver.fill(aliasTable.getAlias(value));
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void enableOfflineMode() {
        ensureLoggedIn();

        applicationPage
                .openSettingsMenu()
                .enableOfflineMode();

        applicationPage.assertOfflineModeLoads();
    }

    @Override
    public OfflineMode getCurrentOfflineMode() {
        ensureLoggedIn();

        return applicationPage.getOfflineMode();
    }

    @Override
    public void synchronize() {
        ensureLoggedIn();

        applicationPage.openSettingsMenu().synchronizeNow();
        applicationPage.assertOfflineModeLoads();
    }

    @Override
    public int countFormSubmissions(String formName) {
        ensureLoggedIn();

        DataEntryTab dataEntryTab = applicationPage.navigateToDataEntryTab();
        currentPage = dataEntryTab.navigateToForm(aliasTable.getAlias(formName));
        
        return dataEntryTab.getCurrentSiteCount();
    }

    @Override
    public File exportForm(String formName) {
        ensureLoggedIn();
        
        DataEntryTab dataEntryTab = applicationPage.navigateToDataEntryTab();
        currentPage = dataEntryTab.navigateToForm(aliasTable.getAlias(formName));
        
        return dataEntryTab.export();
    }

    @Override
    public List<HistoryEntry> getSubmissionHistory() {
        Preconditions.checkState(currentForm != null, "No current form");

        DataEntryTab dataEntryTab = applicationPage.navigateToDataEntryTab();
        currentPage = dataEntryTab.navigateToForm(aliasTable.getAlias(currentForm));
        
        dataEntryTab.selectSubmission(0);
        return dataEntryTab.changes();
    }

    @Override
    public DetailsEntry getDetails() {
        Preconditions.checkState(currentForm != null, "No current form");

        DataEntryTab dataEntryTab = applicationPage.navigateToDataEntryTab();
        currentPage = dataEntryTab.navigateToForm(aliasTable.getAlias(currentForm));
        dataEntryTab.selectSubmission(0);

        return dataEntryTab.details();
    }

    @Override
    public void cloneDatabase(TestObject testObject) {
        ensureLoggedIn();

        BsModal dialog = applicationPage.navigateToDesignTab().newDatabase();

        dialog.form().findFieldByLabel("Use an existing database as a template").getElement().click();
        dialog.click("Next »", "Create a new database");

        dialog.form().findFieldByLabel("Choose a database to copy").select(testObject.getAlias("sourceDatabase"));
        dialog.click("Next »", "Create a new database");

        dialog.form().findFieldByLabel("Name").fill(testObject.getAlias("targetDatabase"));
        dialog.form().findFieldByLabel("Country").select("Rdc");
        dialog.form().findFieldByLabel("Options").select("Copy partners");
        dialog.form().findFieldByLabel("Options").select("Copy user permissions");
        dialog.click("Create");
    }

    @Override
    public void updateSubmission(List<FieldValue> values) throws Exception {

        DataEntryTab dataEntryTab = applicationPage.navigateToDataEntryTab();
        currentPage = dataEntryTab.navigateToForm(aliasTable.getAlias(currentForm));

        dataEntryTab.selectSubmission(0);

        DataEntryDriver driver = dataEntryTab.updateSubmission();
        
        // todo conflict ?
        //fillForm(driver, FieldValue.toMap(values));

        fillForm(FieldValue.toMap(values), driver);

        driver.submit();
    }

    private TargetsPage navigateToTargetSetupFor(String database) {
        ensureLoggedIn();

        currentDatabase = database;

        if(currentPage instanceof TargetsPage) {
            return (TargetsPage) currentPage;
        }
        
        return applicationPage
                    .navigateToDesignTab()
                    .selectDatabase(database)
                    .targets();
    }
    
    @Override
    public void createTarget(TestObject target) throws Exception {
        TargetsPage targetPage = navigateToTargetSetupFor(target.getAlias("database"));

        GxtModal dialog = targetPage.addButton();

        dialog.form().fillTextField("Name", target.getAlias());
        dialog.form().fillDateField("from", new LocalDate(2014, 1, 1));
        dialog.form().fillDateField("to", new LocalDate(2014, 12, 31));

        if (target.has("partner")) {
            dialog.form().select("Partner", target.getAlias("partner"));
        }
        if (target.has("project")) {
            dialog.form().select("Project", target.getAlias("project"));
        }

        dialog.accept();
    }

    public TargetsPage targetsPage() {
        Preconditions.checkState(currentDatabase != null, "No current database");
        return navigateToTargetSetupFor(currentDatabase);
    }

    @Override
    public void setTargetValues(String targetName, List<FieldValue> values) throws Exception {
        TargetsPage targetPage = targetsPage();
        targetsPage().select(aliasTable.getAlias(targetName));
        for(FieldValue value : values) {
            targetPage.setValue(aliasTable.getAlias(value.getField()), value.getValue());   
        }
    }


    @Override
    public DataTable pivotTable(List<String> measures, List<String> rowDimension) {
        ensureLoggedIn();

        PivotTableEditor pivotTable = applicationPage
                .navigateToReportsTab()
                .createPivotTable();

        currentPage = pivotTable;

        for (String measure : measures) {
            pivotTable.selectMeasure(aliasTable.getAlias(measure));
        }

        pivotTable.selectDimensions(rowDimension, Collections.<String>emptyList());
        return pivotTable.extractData();
    }

    @Override
    public DataTable drillDown(String cellValue) {
        Preconditions.checkState(currentPage instanceof PivotTableEditor, "No pivot results. Please pivot data first before using drill down.");

        PivotTableEditor pivotTable = (PivotTableEditor) currentPage;
        DrillDownDialog drillDown = pivotTable.drillDown(cellValue);

        DataTable dataTable = drillDown.table().
                waitUntilReloadedSilently().
                waitUntilAtLeastOneRowIsLoaded().
                extractData(false);
        drillDown.close();
        return dataTable;
    }

    @Override
    public void assertVisible(ObjectType objectType, boolean exists, TestObject testObject) {
        ensureLoggedIn();

        String name = testObject.getAlias("name");

        DesignTab designTab = applicationPage.navigateToDesignTab();
        designTab.selectDatabase(testObject.getAlias("database"));

        if (objectType == ObjectType.LOCATION_TYPE || objectType == ObjectType.FORM) {
            Optional<GxtTree.GxtNode> node = designTab.design().getDesignTree().search(name);

            if (exists) {
                Assert.assertTrue(objectType.name() + " with name '" + name + "' is not present.", node.isPresent());
            } else {
                Assert.assertTrue(objectType.name() + " with name '" + name + "' is present.", !node.isPresent());
            }
        } else if (objectType == ObjectType.PARTNER) {
            GxtGrid grid = designTab.partners().grid();
            grid.findCell(testObject.getAlias("name"));

        } else if (objectType == ObjectType.FORM_FIELD) {
            DesignPage designPage = designTab.design();
            designPage.getDesignTree().search(name).get().select();
            designPage.getToolbarMenu().clickButton("Open Table");

            designTab.formInstanceTable().getToolbarMenu().clickButton("New");

            BsModal dialog = designTab.formInstance();
            BsFormPanel.BsField bsField = (BsFormPanel.BsField) dialog.form().findFieldByLabel(testObject.getString("formFieldName"));

            List<String> items = testObject.getAliasList(testObject.getStringList("items"));
            if (!bsField.itemLabels().containsAll(items)) {
                throw new AssertionError("Not all elements formfield elements are present, expected: "
                        + Joiner.on(",").join(items) + ", actual: " + Joiner.on(",").join(bsField.itemLabels()));
            }
        }
    }

    public void assertTargetValues(String targetName, List<FieldValue> targetValues) {
        Preconditions.checkState(currentDatabase != null, "No current database");
        TargetsPage targetPage = navigateToTargetSetupFor(currentDatabase);
        targetPage.select(aliasTable.getAlias(targetName));

        for (FieldValue value : targetValues) {
            targetPage.expandTree(value.getField());
            targetPage.valueGrid().findCell(value.getValue());
        }
    }

    @Override
    public void delete(ObjectType objectType, TestObject testObject) throws Exception {
        switch (objectType) {
            case LOCATION_TYPE:
                deleteLocationType(testObject);
                break;
            case TARGET:
                deleteTarget(testObject);
                break;
            default:
                throw new IllegalArgumentException(String.format("Invalid object type '%s'", objectType));
        }
    }

    private void deleteTarget(TestObject testObject) {
        DesignTab designTab = applicationPage.navigateToDesignTab();
        designTab.selectDatabase(testObject.getAlias("database"));

        TargetsPage targetsPage = designTab.targets();
        targetsPage.select(testObject.getAlias("name"));
        targetsPage.getToolbarMenu().clickButton("Delete");

    }

    private void deleteLocationType(TestObject testObject) {
        DesignTab designTab = applicationPage.navigateToDesignTab();
        designTab.selectDatabase(testObject.getAlias("database"));

        DesignPage designPage = designTab.design();
        designPage.getDesignTree().select(testObject.getAlias("name"));
        designPage.getToolbarMenu().clickButton("Delete");
    }

    @Override
    public LinkIndicatorsPage getLinkIndicatorPage() {
        ensureLoggedIn();

        return applicationPage.navigateToDesignTab().linkIndicators();
    }

    @Override
    public FormDesignerPage openFormDesigner(String database, String formName) {
        ensureLoggedIn();

        currentPage = applicationPage.navigateToFormDesigner(aliasTable.getAlias(database), aliasTable.getAlias(formName));
        return (FormDesignerPage) currentPage;
    }

    public TablePage openFormTable(String database, String formName) {
        ensureLoggedIn();

        currentPage = applicationPage.navigateToTable(database, formName);
        return (TablePage) currentPage;
    }

    public void assertFieldVisible(String formName, String databaseName, String fieldName, String controlType) {
        TablePage tablePage = openFormTable(aliasTable.getAlias(databaseName), aliasTable.getAlias(formName));
        BsModal modal = tablePage.table().newSubmission();
        Form.FormItem fieldByLabel = modal.form().findFieldByLabel(fieldName);

        assertNotNull(fieldByLabel);
        if (ControlType.fromValue(controlType) == ControlType.SUGGEST_BOX) {
            assertEquals(fieldByLabel.getPlaceholder(), I18N.CONSTANTS.suggestBoxPlaceholder());
        }
    }

    @Override
    public void addLockOnDb(String lockName, String database, String startDate, String endDate, boolean lockActive) {
        ensureLoggedIn();

        LocksPage locksPage = applicationPage.navigateToDesignTab().selectDatabase(aliasTable.getAlias(database)).locks();
        LocksDialog lockDialog = locksPage.addLock();
        lockDialog.selectDatabase().
                name(lockName).
                active(lockActive).
                startDate(LocalDate.parse(startDate)).
                endDate(LocalDate.parse(endDate)).
                getModal().
                accept();
    }

    @Override
    public void addLockOnForm(String lockName, String database, String formName, String startDate, String endDate, boolean lockActive) {
        ensureLoggedIn();

        LocksPage locksPage = applicationPage.navigateToDesignTab().selectDatabase(aliasTable.getAlias(database)).locks();
        LocksDialog lockDialog = locksPage.addLock();
        lockDialog.selectForm(aliasTable.getAlias(formName)).
                name(lockName).
                active(lockActive).
                startDate(LocalDate.parse(startDate)).
                endDate(LocalDate.parse(endDate)).
                getModal().
                accept();
    }

    public void addLockOnProject(String lockName, String database, String projectName, String startDate, String endDate, boolean lockActive) {
        ensureLoggedIn();

        LocksPage locksPage = applicationPage.navigateToDesignTab().selectDatabase(aliasTable.getAlias(database)).locks();
        LocksDialog lockDialog = locksPage.addLock();
        lockDialog.selectProject(aliasTable.getAlias(projectName)).
                name(lockName).
                active(lockActive).
                startDate(LocalDate.parse(startDate)).
                endDate(LocalDate.parse(endDate)).
                getModal().
                accept();
    }

    /**
     * Asserts that submission is not allowed because of lock. To check it all required fields must be filled, therefore
     * it assumes that project with name "Project1" is pre-created.
     * @param formName new form
     * @param endDate end date in format yyyy-MM-dd
     */
    public void assertSubmissionIsNotAllowedBecauseOfLock(String formName, String endDate) {
        final DataEntryTab dataEntryTab = applicationPage.navigateToDataEntryTab().navigateToForm(aliasTable.getAlias(formName));
        GxtDataEntryDriver dataEntryDriver = dataEntryTab.newSubmission();

        // we have to fill all required fields first in order to make sure that submission is not allowed because
        // of locking (and not because of some missed required field)
        while (dataEntryDriver.nextField()) {
            String label = dataEntryDriver.getLabel();
            switch (label) {
                case "Partner":
                    dataEntryDriver.select("Default");
                    break;
                case "Project":
                    dataEntryDriver.select(aliasTable.getAlias("Project1"));
                    break;
                case "Start Date":
                    dataEntryDriver.fill(LocalDate.parse(endDate));
                    break;
                case "End Date":
                    dataEntryDriver.fill(LocalDate.parse(endDate));
                    dataEntryDriver.sendKeys(Keys.TAB);

                    FluentWait<GxtDataEntryDriver> wait = new FluentWait<>(dataEntryDriver).
                            withTimeout(3, TimeUnit.SECONDS);
                    wait.ignoring(WebDriverException.class);
                    wait.until(new Predicate<GxtDataEntryDriver>() {
                        @Override
                        public boolean apply(GxtDataEntryDriver input) {
                            // click anywhere to activate gxt validator which is run only when field lost focus
                            GxtModal.waitForModal(dataEntryTab.getContainer()).getWindowElement().click();
                            return !input.isValid();
                        }
                    });
                    return; // success, field is marked as not valid and therefore submission is not possible
            }
        }
        throw new AssertionError("New submission is still possible for form: " + formName +
                " (however it has to be locked.)");
    }

    public void assertEntryCannotBeModifiedOrDeleted(String databaseNameOrFormName, List<FieldValue> values) {
        DataEntryTab dataEntryTab = applicationPage.navigateToDataEntryTab();
        List<DetailsEntry> detailsEntries = collectDetailsForForm(databaseNameOrFormName, dataEntryTab, -1, 1);

        aliasTable.deAliasDetails(detailsEntries);

        for (FieldValue value : values) {
            assertEntryIsLocked(dataEntryTab, detailsEntries, value);
        }
    }

    private void assertEntryIsLocked(DataEntryTab dataEntryTab, List<DetailsEntry> detailsEntries, FieldValue value) {
        int row = findMatchedRow(detailsEntries, value);
        dataEntryTab.selectSubmission(row);

        // locked on edit
        dataEntryTab.buttonClick(I18N.CONSTANTS.edit());
        assertLockedSiteDialog(dataEntryTab);

        // locked on delete
        dataEntryTab.buttonClick(I18N.CONSTANTS.delete());
        assertLockedSiteDialog(dataEntryTab);
    }

    private void assertLockedSiteDialog(DataEntryTab dataEntryTab) {
        GxtModal gxtModal = GxtModal.waitForModal(dataEntryTab.getContainer());
        String dialogTitle = gxtModal.getTitle().trim();
        assertEquals("Entry is not locked. Dialog title: "
                        + dialogTitle + ", expected: " + I18N.CONSTANTS.lockedSiteTitle(),
                dialogTitle, I18N.CONSTANTS.lockedSiteTitle());
        gxtModal.clickButton("OK");
    }

    private int findMatchedRow(List<DetailsEntry> detailsEntries, FieldValue value) {
        int row = 0;
        for (DetailsEntry details : detailsEntries) {
            if (details.getFieldValues().contains(value)) {
                return row;
            }
            row++;
        }
        throw new AssertionError("Failed to find matched row for \n Value : " + value +
                "\n Details on UI: " + DetailsEntry.toString(detailsEntries));
    }


    @Override
    public void createLinkIndicators(List<IndicatorLink> linkedIndicatorRows) {
        LinkIndicatorsPage linkIndicatorsPage = getLinkIndicatorPage();
        linkIndicatorsPage.getSourceDb().waitUntilAtLeastOneRowIsLoaded();
        linkIndicatorsPage.getTargetDb().waitUntilAtLeastOneRowIsLoaded();

        for (IndicatorLink row : linkedIndicatorRows) {

            linkIndicatorsPage.getSourceDb().findCell(aliasTable.getAlias(row.getSourceDb())).click();
            linkIndicatorsPage.getTargetDb().findCell(aliasTable.getAlias(row.getDestDb())).click();

            Tester.sleepSeconds(1); // sometimes it's too fast and we have to give time show "Loading" and only then wait for rows

            GxtGrid sourceIndicator = linkIndicatorsPage.getSourceIndicator().waitUntilAtLeastOneRowIsLoaded();
            GxtGrid targetIndicator = linkIndicatorsPage.getTargetIndicator().waitUntilAtLeastOneRowIsLoaded();

            sourceIndicator.findCell(aliasTable.getAlias(row.getSourceIndicator())).click();
            targetIndicator.findCell(aliasTable.getAlias(row.getDestIndicator())).click();

            linkIndicatorsPage.clickLinkButton();
        }
    }

    public void assertLinkedIndicatorsMarked(List<IndicatorLink> linkedIndicatorRows, boolean marked) {
        LinkIndicatorsPage linkIndicatorsPage = getLinkIndicatorPage();
        linkIndicatorsPage.getSourceDb().waitUntilAtLeastOneRowIsLoaded();

        for (IndicatorLink row : linkedIndicatorRows) {

            Preconditions.checkState(linkIndicatorsPage.getSourceDb().findCell(aliasTable.getAlias(row.getSourceDb())).hasIcon(), marked);
            Preconditions.checkState(linkIndicatorsPage.getTargetDb().findCell(aliasTable.getAlias(row.getDestDb())).hasIcon(), marked);

            GxtGrid sourceIndicator = linkIndicatorsPage.getSourceIndicator().waitUntilAtLeastOneRowIsLoaded();
            GxtGrid targetIndicator = linkIndicatorsPage.getTargetIndicator().waitUntilAtLeastOneRowIsLoaded();

            Preconditions.checkState(sourceIndicator.findCell(aliasTable.getAlias(row.getSourceIndicator())).hasIcon(), marked);
            Preconditions.checkState(targetIndicator.findCell(aliasTable.getAlias(row.getDestIndicator())).hasIcon(), marked);
        }
    }

    public void assertDataEntryTableForForm(String formName, DataTable expectedTable) {
        ensureLoggedIn();

        DataEntryTab dataEntryTab = applicationPage.navigateToDataEntryTab();
        currentPage = dataEntryTab.navigateToForm(aliasTable.getAlias(formName));

        List<DetailsEntry> detailsEntries = collectDetailsForForm(formName, dataEntryTab, expectedTable.getGherkinRows().size() - 1, 1);
        for (DetailsEntry entry : detailsEntries) {
            aliasTable.deAlias(entry.getFieldValues());
        }

        assertTableEquals(expectedTable, detailsEntries);
    }

    /**
     * Because we navigate form in tree sites may come out of order which leads to test failures. Therefore if
     * number of details does not match number of expected details then we retry.
     *
     * @param dataEntryTab data entry tab
     * @param expectedNumberOfDetails expected number of details, -1 if we should ignore it
     * @param retry retry count
     * @return collected detail entries
     */
    private List<DetailsEntry> collectDetailsForForm(String formName, DataEntryTab dataEntryTab, int expectedNumberOfDetails, int retry) {
        currentPage = dataEntryTab.navigateToForm(aliasTable.getAlias(formName));

        List<DetailsEntry> detailsEntries = collectDetails(dataEntryTab);
        if (detailsEntries.size() == expectedNumberOfDetails) {
            return detailsEntries;
        }
        if (expectedNumberOfDetails == -1) {
            if (detailsEntries.size() > 0) {
                return detailsEntries;
            } else {
                throw new AssertionError("Failed to fetch any details for form: " + formName);
            }
        }
        int retryLimit = 3;
        if (retry > retryLimit) {
            throw new AssertionError("Failed to fetch details for form: " + formName +
                    ", expected details: " + expectedNumberOfDetails + " but got: " + detailsEntries.size());
        }
        retry++;
        return collectDetailsForForm(formName, dataEntryTab, expectedNumberOfDetails, retry);
    }

    public static void assertTableEquals(DataTable expectedTable, List<DetailsEntry> detailsEntries) {
        List<DataTableRow> matchedRows = Lists.newArrayList();
        List<DetailsEntry> matchedDetailsEntries = Lists.newArrayList();
        for (int i = 1; i < expectedTable.getGherkinRows().size(); i++) {
            DataTableRow row = expectedTable.getGherkinRows().get(i);
            if (matchedRows.contains(row)) {
                continue;
            }
            for (DetailsEntry detailsEntry : detailsEntries) {
                if (matchedDetailsEntries.contains(detailsEntry)) {
                    continue;
                }
                if (equals(expectedTable.getGherkinRows().get(0).getCells(), row, detailsEntry.getFieldValues())) {
                    matchedRows.add(row);
                    matchedDetailsEntries.add(detailsEntry);
                    break;
                }
            }
        }

        if (matchedRows.size() != (expectedTable.getGherkinRows().size() - 1)) { // -1 because of first row is header
            List<DataTableRow> notMatched = Lists.newArrayList(expectedTable.getGherkinRows());
            notMatched.remove(expectedTable.getGherkinRows().get(0)); // remove header
            notMatched.removeAll(matchedRows);

            String notMatchedString = "";
            for (DataTableRow row : notMatched) {
                notMatchedString += Joiner.on(" | ").join(row.getCells()) + "\n";
            }

            throw new AssertionError("Data entry table does not match. Expected: \n"
                    + expectedTable + "\n But got: \n" + DetailsEntry.toString(detailsEntries) + "\n Not matched rows:\n" + notMatchedString);
        }
    }

    public static boolean equals(List<String> columns, DataTableRow gherkinRow, List<FieldValue> values) {
        Set<Integer> matchedCellIndexes = Sets.newHashSet();
        for (FieldValue value : values) {

            for (int column = 0; column < gherkinRow.getCells().size(); column++) {
                if (matchedCellIndexes.contains(column)) {
                    continue;
                }

                String cell = gherkinRow.getCells().get(column);
                if (cell.isEmpty() ||
                        (cell.equals(value.getValue()) && columns.get(column).equals(value.getField()))) {
                    matchedCellIndexes.add(column);
                }
            }
        }

        return matchedCellIndexes.size() == gherkinRow.getCells().size();
    }

    private List<DetailsEntry> collectDetails(DataEntryTab dataEntryTab) {
        List<DetailsEntry> result = Lists.newArrayList();
        int row = 0;
        try {
            while (true) {
                dataEntryTab.selectSubmission(row);
                result.add(dataEntryTab.details());
                row++;
                if (row > 10000) { // safe escape
                    throw new AssertionError("Failed to fetch details for submissions on Data Entry tab.");
                }
            }
        } catch (IndexOutOfBoundsException e) {
            // no rows anymore
        }
        return result;
    }

    public void assertDesignerFieldVisible(String fieldLabel) {
        assertNotNull("Failed to find designer field with label: " + fieldLabel,
                formDesigner().dropTarget().fieldByLabel(fieldLabel));
    }

    private FormDesignerPage formDesigner() {
        Preconditions.checkState(currentPage instanceof FormDesignerPage, "Form Designer must be open before with 'I open form designer ...' step.");

        return (FormDesignerPage) currentPage;
    }

    public void assertDesignerFieldIsNotDeletable(String fieldLabel) {
        Preconditions.checkState(!formDesigner().dropTarget().fieldByLabel(fieldLabel).isDeletable(),
                "Field with label '" + fieldLabel +"' is deletable.");
    }

    @Override
    public void assertDesignerFieldHasProperty(String fieldLabel, DesignerFieldPropertyType fieldPropertyType, boolean enabled) {
        DesignerField designerField = formDesigner().dropTarget().fieldByLabel(fieldLabel);
        designerField.element().clickWhenReady();
        PropertiesPanel propertiesPanel = formDesigner().properties();

        final boolean actualValue;
        switch (fieldPropertyType) {
            case RELEVANCE:
                FluentElement propertyElement = propertiesPanel.getContainer().find().
                        label(XPathBuilder.containingText(I18N.CONSTANTS.relevance())).first();
                actualValue = propertyElement.isDisplayed();
                break;
            case REQUIRED:
                actualValue = propertiesPanel.form().findFieldByLabel(I18N.CONSTANTS.required()).isEnabled();
                break;
            case VISIBLE:
                actualValue = propertiesPanel.form().findFieldByLabel(I18N.CONSTANTS.visible()).isEnabled();
                break;
            default:
                throw new AssertionError("Unsupported field property type: " + fieldPropertyType);
        }

        if (actualValue != enabled) {
            throw new AssertionError("'" + fieldPropertyType + "' field property state is " + actualValue +
                    " while it's expected to have it " + enabled + " for label: " + fieldLabel);
        }

    }

    public void assertDesignerFieldMandatory(String fieldLabel) {
        assertTrue("Designer field with label " + fieldLabel + " is not mandatory",
                formDesigner().dropTarget().fieldByLabel(fieldLabel).isMandatory());
    }

    public void changeDesignerField(String fieldLabel, List<FieldValue> values) {
        formDesigner().dropTarget().fieldByLabel(fieldLabel).element().clickWhenReady();
        BsFormPanel form = formDesigner().properties().form();
        for (FieldValue value : values) {
            switch(value.getField()) {
                case "code":
                    form.findFieldByLabel(I18N.CONSTANTS.codeFieldLabel()).fill(value.getValue());
                    break;
                case "label":
                    form.findFieldByLabel(I18N.CONSTANTS.labelFieldLabel()).fill(value.getValue());
                    break;
                case "description":
                    form.findFieldByLabel(I18N.CONSTANTS.description()).fill(value.getValue());
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown designer field property: " + value.getField());
            }
        }
    }

    public void assertDesignerFieldReorder(String fieldLabel, int positionOnPanel) {
        int positionBeforeReorder = formDesigner().dropTarget().fieldPosition(fieldLabel);
        formDesigner().dropTarget().dragAndDrop(fieldLabel, positionOnPanel);

        int positionAfterReorder = formDesigner().dropTarget().fieldPosition(fieldLabel);
        assertEquals(positionAfterReorder, positionOnPanel);
        assertNotEquals(positionBeforeReorder, positionAfterReorder); // make sure field is really reordered
    }

    public void assertFieldsOnNewForm(String formName, List<String> fieldLabels) {
        ensureLoggedIn();

        DataEntryTab dataEntryTab = applicationPage.navigateToDataEntryTab().navigateToForm(aliasTable.getAlias(formName));
        dataEntryTab.buttonClick(I18N.CONSTANTS.newSite());

        BsModal bsModal = FormModal.find(dataEntryTab.getContainer());
        for (String fieldLabel : fieldLabels) {
            assertNotNull("Failed to find field with label: " + fieldLabel, bsModal.form().findFieldByLabel(fieldLabel));
        }
    }

    public void assertFieldValuesOnNewForm(String formName, List<FieldValue> values) {
        ensureLoggedIn();

        DataEntryTab dataEntryTab = applicationPage.navigateToDataEntryTab().navigateToForm(aliasTable.getAlias(formName));
        dataEntryTab.buttonClick(I18N.CONSTANTS.newSite());

        BsModal bsModal = FormModal.find(dataEntryTab.getContainer());
        for (FieldValue field : values) {
            BsFormPanel.BsField fieldItem = (BsFormPanel.BsField) bsModal.form().findFieldByLabel(field.getField());
            switch (field.getControlType()) {
                case "radio":
                    assertTrue(fieldItem.isRadioSelected(field.getValue()));
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown control type: " + field.getControlType());
            }
        }
    }

    public Object getCurrentPage() {
        return currentPage;
    }

    @Override
    public void cleanup() throws Exception {
        setup().cleanup();
    }
}
