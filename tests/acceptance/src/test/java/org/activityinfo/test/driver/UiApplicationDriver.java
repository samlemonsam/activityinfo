package org.activityinfo.test.driver;

import cucumber.api.DataTable;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.activityinfo.test.pageobject.gxt.GxtModal;
import org.activityinfo.test.pageobject.web.ApplicationPage;
import org.activityinfo.test.pageobject.web.LoginPage;
import org.activityinfo.test.pageobject.web.design.TargetsPage;
import org.activityinfo.test.pageobject.web.reports.PivotTableEditor;
import org.activityinfo.test.sut.UserAccount;
import org.joda.time.LocalDate;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;


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
        // defer actually logging in until the first command so that we can
        // let the setup steps execute first
        this.currentUser = account;

        setup().login(account);
    }
    
    private void ensureLoggedIn() {
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
