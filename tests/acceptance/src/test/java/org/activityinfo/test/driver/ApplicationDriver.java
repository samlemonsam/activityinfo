package org.activityinfo.test.driver;


import com.google.common.collect.Lists;
import cucumber.api.DataTable;
import cucumber.api.PendingException;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.time.LocalDateType;
import org.activityinfo.test.driver.model.IndicatorLink;
import org.activityinfo.test.pageobject.web.design.LinkIndicatorsPage;
import org.activityinfo.test.pageobject.web.design.designer.DesignerFieldPropertyType;
import org.activityinfo.test.pageobject.web.design.designer.FormDesignerPage;
import org.activityinfo.test.pageobject.web.entry.DetailsEntry;
import org.activityinfo.test.pageobject.web.entry.HistoryEntry;
import org.activityinfo.test.pageobject.web.entry.TablePage;
import org.activityinfo.test.sut.UserAccount;
import org.joda.time.LocalDate;
import org.json.JSONException;

import java.io.File;
import java.util.List;
import java.util.Map;

public abstract class ApplicationDriver {

    private final AliasTable aliasTable;

    public ApplicationDriver(AliasTable aliasTable) {
        this.aliasTable = aliasTable;
    }

    /**
     * Login as any user
     */
    public abstract void login();
    
    public abstract void login(UserAccount account);

    /**
     * @return an implementation of ApplicationDriver suitable for setting up a test scenario
     */
    public ApplicationDriver setup() {
        return this;
    }

    public String resolveFieldTypeName(String type) {
        if (type.equalsIgnoreCase("enum")) { // trick to not write long work enumerated in *.feature file
            type = EnumType.TYPE_CLASS.getId();
        } else if (type.equalsIgnoreCase("text")) { // trick to not write long work free_text in *.feature file
            type = TextType.TYPE_CLASS.getId();
        } else if (type.equalsIgnoreCase("date")) {
            type = LocalDateType.TYPE_CLASS.getId();
        }
        return type;
    }
    
    public final void createDatabase(Property... properties) throws Exception {
        createDatabase(new TestObject(aliasTable, properties));
    }
    
    public void enableOfflineMode() {
        throw new UnsupportedOperationException();
    }
    
    public OfflineMode getCurrentOfflineMode() {
        throw new UnsupportedOperationException();
    }

    protected void createDatabase(TestObject database) throws Exception {
        throw new PendingException();
    }

    public final void createForm(Property... properties) throws Exception {
        createForm(new TestObject(aliasTable, properties));
    }

    protected void createForm(TestObject form) throws Exception {
        throw new PendingException();
    }

    public void createField(Property... properties) throws Exception {
        createField(new TestObject(aliasTable, properties));
    }

    public void createField(TestObject testObject) throws Exception {
        throw new PendingException();
    }

    public void submitForm(String formName, List<FieldValue> values) throws Exception {
        fillForm(startNewSubmission(formName), FieldValue.toMap(values));
    }
    
    protected final void fillForm(DataEntryDriver driver, Map<String, FieldValue> valueMap) throws InterruptedException {
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

            driver.submit();
        }
    }

    public void submitForm(String formName, List<FieldValue> values, List<String> headers) throws Exception {
        throw new PendingException();
    }

    protected DataEntryDriver startNewSubmission(String formName) {
        throw new PendingException();
    }
    
    public void submitForm(String formName, String partner, List<MonthlyFieldValue> fieldValues) throws JSONException, Exception {
        throw new PendingException();
    }


    public void delete(ObjectType objectType, String name) throws Exception {
        throw new PendingException();
    }

    public void delete(ObjectType objectType, Property... properties) throws Exception {
        delete(objectType, new TestObject(aliasTable, properties));
    }

    public void delete(ObjectType objectType, TestObject testObject) throws Exception {
        throw new PendingException();
    }

    public void addPartner(String partnerName, String databaseName) throws  Exception {
        throw new PendingException();
    }

    public void createTarget(Property... properties) throws Exception {
        createTarget(new TestObject(aliasTable, properties));
    }

    protected void createTarget(TestObject target) throws Exception {
        throw new PendingException();
    }

    public void setTargetValues(String targetName, List<FieldValue> values) throws Exception {
        throw new PendingException();
    }

    public final void createProject(Property... properties) throws Exception {
        createProject(new TestObject(aliasTable, properties));
    }

    protected void createProject(TestObject project) throws Exception {
        throw new PendingException();
    }

    public DataTable pivotTable(String measure, List<String> rowDimensions) throws Exception {
        return pivotTable(Lists.newArrayList(measure), rowDimensions);
    }

    public DataTable pivotTable(List<String> measure, List<String> rowDimensions) throws Exception {
        throw new PendingException();
    }

    public final void grantPermission(Property... properties) throws Exception {
        grantPermission(new TestObject(aliasTable, properties));
    }

    public DataTable drillDown(String cellValue) {
        throw new PendingException();
    }

    protected void grantPermission(TestObject permission) throws Exception {
        throw new PendingException();
    }

    public void cleanup() throws Exception {
        
    }

    public final void createLocationType(Property... properties) throws Exception {
        createLocationType(new TestObject(aliasTable, properties));
    }

    protected void createLocationType(TestObject testObject) throws Exception {
        throw new PendingException();
    }

    public final void createLocation(Property... properties) throws Exception {
        createLocation(new TestObject(aliasTable, properties));
        
    }

    protected void createLocation(TestObject testObject) throws Exception {
        throw new PendingException();
    }

    public void assertVisible(ObjectType objectType, boolean exists, Property... properties) {
        assertVisible(objectType, exists, new TestObject(aliasTable, properties));
    }

    public void assertVisible(ObjectType objectType, boolean exists, TestObject testObject) {
        throw new PendingException();
    }

    public void synchronize() {
        throw new UnsupportedOperationException();
    }

    public int countFormSubmissions(String formName) {
        throw new PendingException();
    }

    public File exportForm(String formName) throws Exception {
        throw new PendingException();
    }
    
    public File exportDatabase(String databaseName) throws Exception {
        throw new PendingException();
    }

    /**
     * @return the text of the history of the last site we modified
     */
    public List<HistoryEntry> getSubmissionHistory() {
        throw new PendingException();
    }

    public void updateSubmission(List<FieldValue> values) throws InterruptedException, Exception {
        throw new PendingException();
    }

    public DetailsEntry getDetails() {
        throw new PendingException();
    }

    public AliasTable getAliasTable() {
        return aliasTable;
    }


    public void cloneDatabase(TestObject testObject) {
        throw new PendingException();
    }

    public void assertTargetValues(String targetName, List<FieldValue> targetValues) {
        throw new PendingException();
    }

    public LinkIndicatorsPage getLinkIndicatorPage() {
        throw new PendingException();
    }

    public void createLinkIndicators(List<IndicatorLink> linkedIndicatorRows) {
        throw new PendingException();
    }

    public void assertLinkedIndicatorsMarked(List<IndicatorLink> linkedIndicatorRows, boolean marked) {
        throw new PendingException();
    }

    public void assertDataEntryTableForForm(String formName, DataTable expectedTable) {
        throw new PendingException();
    }

    public FormDesignerPage openFormDesigner(String database, String formName) {
        throw new PendingException();
    }

    public TablePage openFormTable(String database, String formName) {
        throw new PendingException();
    }

    public void assertFieldVisible(String formName, String databaseName, String fieldName, String controlType) {
        throw new PendingException();
    }

    public void addLockOnDb(String lockName, String database, String startDate, String endDate, boolean lockActive) {
        throw new PendingException();
    }

    public void addLockOnForm(String lockName, String database, String formName, String startDate, String endDate, boolean lockActive) {
        throw new PendingException();
    }

    public void addLockOnProject(String lockName, String database, String projectName, String startDate, String endDate, boolean lockActive) {
        throw new PendingException();
    }

    public void assertEntryCannotBeModifiedOrDeleted(String databaseName, List<FieldValue> values) {
        throw new PendingException();
    }

    public void assertSubmissionIsNotAllowedBecauseOfLock(String formName, String endDate) {
        throw new PendingException();
    }

    public void assertDesignerFieldVisible(String fieldLabel) {
        throw new PendingException();
    }

    public void assertFieldsOnNewForm(String formName, List<String> fieldLabels) {
        throw new PendingException();
    }

    public void assertDesignerFieldIsNotDeletable(String fieldLabel) {
        throw new PendingException();
    }

    public void assertDesignerFieldReorder(String fieldLabel, int positionOnPanel) {
        throw new PendingException();
    }

    public void assertDesignerFieldMandatory(String fieldLabel) {
        throw new PendingException();
    }

    public void changeDesignerField(String fieldLabel, List<FieldValue> values) {
        throw new PendingException();
    }

    public void assertFieldValuesOnNewForm(String formName, List<FieldValue> values) {
        throw new PendingException();
    }

    public Object getCurrentPage() {
        throw new PendingException();
    }

    public void assertDesignerFieldHasProperty(String fieldLabel, DesignerFieldPropertyType fieldPropertyType, boolean enabled) {
        throw new PendingException();
    }
    
}
