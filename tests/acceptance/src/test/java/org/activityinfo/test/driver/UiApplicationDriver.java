package org.activityinfo.test.driver;

import cucumber.api.DataTable;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.activityinfo.test.pageobject.gxt.GxtModal;
import org.activityinfo.test.pageobject.web.ApplicationPage;
import org.activityinfo.test.pageobject.web.LoginPage;
import org.activityinfo.test.pageobject.web.design.TargetsPage;
import org.activityinfo.test.pageobject.web.entry.DataEntryTab;
import org.activityinfo.test.pageobject.web.reports.PivotTableEditor;
import org.activityinfo.test.sut.UserAccount;
import org.joda.time.LocalDate;

import javax.inject.Inject;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@ScenarioScoped
public class UiApplicationDriver extends ApplicationDriver {

    private ApiApplicationDriver apiDriver;
    private LoginPage loginPage;

    private AliasTable aliasTable;
    
    private UserAccount currentUser = null;
    
    private ApplicationPage applicationPage;
    private TargetsPage targetPage;

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
        if(currentUser == null || !currentUser.getEmail().equals(account.getEmail())) {
            currentUser = account;
            applicationPage = null;

            // defer actually logging in until the first command so that we can
            // let the setup steps execute first
            this.currentUser = account;

            setup().login(account);
        }
    }
    
    public void ensureLoggedIn() {
        if(applicationPage == null) {
            applicationPage = loginPage.navigateTo().loginAs(currentUser).andExpectSuccess();
            applicationPage.waitUntilLoaded();
        }
    }

    @Override
    public ApplicationDriver setup() {
        return apiDriver;
    }


    @Override
    public void submitForm(String formName, List<FieldValue> values) throws Exception {
        ensureLoggedIn();

        Map<String, FieldValue> valueMap = FieldValue.toMap(values);

        DataEntryTab dataEntryTab = applicationPage.navigateToDataEntryTab();
        dataEntryTab.navigateToForm(aliasTable.getAlias(formName));
        
        DataEntryDriver driver = dataEntryTab.newSubmission();
        
        while(driver.nextField()) {
            System.out.println("label = " + driver.getLabel());
            switch(driver.getLabel()) {
                case "Partner":
                    driver.select(aliasTable.getAlias(valueMap.get("partner").getValue()));
                    break;
                case "Start Date":
                    driver.fill(new LocalDate(2014,1,1));
                    break;
                case "End Date":
                    driver.fill(new LocalDate(2014,1,1));
                    break;
                case "Comments":
                    if(valueMap.containsKey("comments")) {
                        driver.fill(valueMap.get("comments").getValue());
                    }
                    break;
                default:
                    String testHandle = aliasTable.getTestHandleForAlias(driver.getLabel());
                    if(valueMap.containsKey(testHandle)) {
                        driver.fill(valueMap.get(testHandle).getValue());
                    }
                    break;
            }
        }
        driver.submit();
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
        dataEntryTab.navigateToForm(aliasTable.getAlias(formName));
        return dataEntryTab.getCurrentSiteCount();
    }

    @Override
    public File exportForm(String formName) {
        DataEntryTab dataEntryTab = applicationPage.navigateToDataEntryTab();
        dataEntryTab.navigateToForm(aliasTable.getAlias(formName));
        return dataEntryTab.export();
    }

    @Override
    public void createTarget(TestObject target) throws Exception {
        ensureLoggedIn();

        targetPage = applicationPage
                .navigateToDesignTab()
                .selectDatabase(target.getAlias("database"))
                .targets();
        
        GxtModal dialog = targetPage.add();
        
        dialog.form().fillTextField("Name", target.getAlias());
        dialog.form().fillDateField("from", new LocalDate(2014, 1, 1));
        dialog.form().fillDateField("to", new LocalDate(2014, 12, 31));
        
        if(target.has("partner")) {
            dialog.form().select("Partner", target.getAlias("partner"));
        }
        if(target.has("project")) {
            dialog.form().select("Project", target.getAlias("project"));
        }
        
        dialog.accept();
    }

    @Override
    public void setTargetValues(String targetName, List<FieldValue> values) throws Exception {
        targetPage.select(targetName);
        for(FieldValue value : values) {
            targetPage.setValue(aliasTable.getAlias(value.getField()), value.getValue());   
        }
    }


    @Override
    public DataTable pivotTable(String measure, List<String> rowDimension) {
        PivotTableEditor pivotTable = applicationPage
                .navigateToReportsTab()
                .createPivotTable();
        
        pivotTable.selectMeasure(aliasTable.getAlias(measure));
        pivotTable.selectDimensions(rowDimension, Collections.<String>emptyList());
        return pivotTable.extractData();
    }


    @Override
    public void cleanup() throws Exception {
        setup().cleanup();
    }
}
