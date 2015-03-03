package org.activityinfo.test.driver;

import cucumber.api.DataTable;
import cucumber.api.PendingException;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.activityinfo.test.pageobject.api.PageBinder;
import org.activityinfo.test.pageobject.gxt.GxtModal;
import org.activityinfo.test.pageobject.web.ApplicationPage;
import org.activityinfo.test.pageobject.web.LoginPage;
import org.activityinfo.test.pageobject.web.design.TargetsPage;
import org.activityinfo.test.pageobject.web.reports.PivotTableEditor;
import org.activityinfo.test.sut.UserAccount;
import org.joda.time.LocalDate;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;


@ScenarioScoped
public class UiApplicationDriver implements ApplicationDriver {

    private ApiApplicationDriver apiDriver;
    private PageBinder pageBinder;
    
    private AliasTable aliasTable;
    
    private UserAccount currentUser = null;
    
    private ApplicationPage applicationPage;
    private TargetsPage targetPage;

    @Inject
    public UiApplicationDriver(ApiApplicationDriver apiDriver, WebDriver webDriver, PageBinder pageBinder, 
                               AliasTable aliasTable) {
        this.apiDriver = apiDriver;
        this.pageBinder = pageBinder;
        this.aliasTable = aliasTable;
    }

    @Override
    public void login() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void login(UserAccount account) {
        // defer actually logging in until the first command so that we can
        // let the setup steps execute first
        this.currentUser = account;

        setup().login(account);
    }
    
    private void ensureLoggedIn() {
        if(applicationPage == null) {
            LoginPage loginPage = pageBinder.navigateTo(LoginPage.class);
            loginPage.loginAs(currentUser);
            loginPage.andExpectSuccess();
            
            applicationPage = pageBinder.waitFor(ApplicationPage.class);
            applicationPage.waitUntilLoaded();
        }
    }

    @Override
    public ApplicationDriver setup() {
        return apiDriver;
    }

    @Override
    public void createDatabase(Property... properties) throws Exception {
        throw new PendingException();
    }

    @Override
    public void createForm(Property... properties) throws Exception {
        throw new PendingException();
    }

    @Override
    public void createField(Property... properties) throws Exception {
        throw new PendingException();
    }

    @Override
    public void submitForm(String formName, List<FieldValue> values) throws Exception {
        throw new PendingException();
    }

    @Override
    public void addPartner(String partnerName, String databaseName) throws Exception {
        throw new PendingException();
    }

    @Override
    public void createTarget(Property... properties) throws Exception {
        TestObject target = new TestObject(properties);
        ensureLoggedIn();

        targetPage = applicationPage
                .navigateToDesignTab()
                .selectDatabase(aliasTable.getName(target.getString("database")))
                .targets();
        
        GxtModal dialog = targetPage.add();
        
        dialog.form().fillTextField("Name", target.getString("name"));
        dialog.form().fillDateField("from", new LocalDate(2014, 1, 1));
        dialog.form().fillDateField("to", new LocalDate(2014, 12, 31));
        
        if(target.has("partner")) {
            dialog.form().select("Partner", aliasTable.getName(target.getString("partner")));
        }
        if(target.has("project")) {
            dialog.form().select("Project", aliasTable.getName(target.getString("project")));
        }
        
        dialog.accept();
        

    }

    @Override
    public void setTargetValues(String targetName, List<FieldValue> values) throws Exception {
        targetPage.select(targetName);
        for(FieldValue value : values) {
            targetPage.setValue(aliasTable.getName(value.getField()), value.asDouble());   
        }
    }

    @Override
    public void createProject(Property... properties) throws Exception {
        throw new PendingException();
    }

    @Override
    public void createLocationType(Property... properties) throws Exception {
        throw new PendingException();
    }

    @Override
    public void createLocation(Property... properties) throws Exception {
        throw new PendingException();
    }

    @Override
    public DataTable pivotTable(String measure, List<String> rowDimension) {
        PivotTableEditor pivotTable = applicationPage
                .navigateToReportsTab()
                .createPivotTable();
        
        pivotTable.selectMeasure(aliasTable.getName(measure));
        pivotTable.selectDimensions(rowDimension, Collections.<String>emptyList());
        return pivotTable.extractData();
    }

    @Override
    public void grantPermission(Property... properties) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(String objectType, String name) throws Exception {
        throw new PendingException();
    }

    @Override
    public void cleanup() throws Exception {
        setup().cleanup();
    }
}
