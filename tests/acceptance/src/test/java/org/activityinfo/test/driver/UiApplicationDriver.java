package org.activityinfo.test.driver;

import com.google.common.base.Optional;
import cucumber.api.DataTable;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.activityinfo.test.pageobject.gxt.GxtModal;
import org.activityinfo.test.pageobject.gxt.GxtTree;
import org.activityinfo.test.pageobject.web.ApplicationPage;
import org.activityinfo.test.pageobject.web.LoginPage;
import org.activityinfo.test.pageobject.web.design.DesignPage;
import org.activityinfo.test.pageobject.web.design.DesignTab;
import org.activityinfo.test.pageobject.web.design.TargetsPage;
import org.activityinfo.test.pageobject.web.reports.PivotTableEditor;
import org.activityinfo.test.sut.UserAccount;
import org.joda.time.LocalDate;
import org.junit.Assert;

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
    public void assertObjectExistence(ObjectType objectType, String objectName, boolean exists) {
        switch(objectType) {
            case LOCATION_TYPE:
                assertLocationTypeExistence(objectName, exists);
                return;
        }
        throw new UnsupportedOperationException("Object type is not supported: " + objectType + ", objectName: " + objectName);
    }

    private void assertLocationTypeExistence(String objectName, boolean exists) {
        DesignTab designTab = applicationPage.navigateToDesignTab();
        designTab.selectDatabase(aliasTable.getAlias("database"));
        Optional<GxtTree.GxtNode> node = designTab.design().getDesignTree().search(objectName);

        if (exists) {
            Assert.assertTrue("Location type with name '" + objectName + "' is not present.", node.isPresent());
        } else {
            Assert.assertTrue("Location type with name '" + objectName + "' is present.", !node.isPresent());
        }
    }

    @Override
    public void delete(ObjectType objectType, String name) throws Exception {
            switch(objectType) {
                case LOCATION_TYPE:
                    deleteLocationType(name);
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Invalid object type '%s'", objectType));
            }
    }

    private void deleteLocationType(String name) {
        DesignTab designTab = applicationPage.navigateToDesignTab();
        designTab.selectDatabase(aliasTable.getAlias("database"));

        DesignPage designPage = designTab.design();
        designPage.getDesignTree().select(name);
        designPage.getToolbarMenu().clickButton("Delete");
    }

    @Override
    public void cleanup() throws Exception {
        setup().cleanup();
    }
}
