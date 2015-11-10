package org.activityinfo.test.ui;

import net.lightoze.gwt.i18n.server.LocaleProxy;
import net.lightoze.gwt.i18n.server.ThreadLocalLocaleProvider;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.driver.UiApplicationDriver;
import org.activityinfo.test.pageobject.gxt.GxtFormPanel;
import org.activityinfo.test.pageobject.web.ApplicationPage;
import org.activityinfo.test.pageobject.web.entry.DataEntryTab;
import org.activityinfo.test.pageobject.web.entry.GxtDataEntryDriver;
import org.activityinfo.test.pageobject.web.entry.LocationDialog;
import org.activityinfo.test.sut.DevServerAccounts;
import org.junit.Test;
import org.openqa.selenium.TimeoutException;

import javax.inject.Inject;
import java.util.Locale;

import static org.activityinfo.test.driver.Property.name;
import static org.activityinfo.test.driver.Property.property;


public class LocationDialogUiTest {

    public static final String CLUSTER_DATABASE = "NFI Cluster";
    public static final String DISTRIBUTION_FORM = "NFI Distribution";
    public static final String VILLAGE = "Village";

    @Inject
    private UiApplicationDriver driver;
    
    @Inject
    private DevServerAccounts accounts;


    @Test
    public void frenchCoordinates() throws Exception {
        LocaleProxy.initialize();
        ThreadLocalLocaleProvider.pushLocale(Locale.forLanguageTag("fr"));
        accounts.setLocale("fr");

        
        System.out.println(I18N.CONSTANTS.newSite());
        
        
        driver.login();
        driver.setup().createDatabase(
                name(CLUSTER_DATABASE));
        
        driver.setup().createLocationType(
                name(VILLAGE), 
                property("database", CLUSTER_DATABASE),
                property("workflowId", "open"));
       
        driver.setup().createForm(name(DISTRIBUTION_FORM),
                property("database", CLUSTER_DATABASE),
                property("locationType", VILLAGE));

        ApplicationPage applicationPage = driver.getApplicationPage();
        DataEntryTab dataEntryTab = applicationPage.navigateToDataEntryTab();

        GxtDataEntryDriver dataEntry = (GxtDataEntryDriver) dataEntryTab
                .navigateToForm(this.driver.getAliasTable().getAlias(DISTRIBUTION_FORM))
                .newSubmission();

        LocationDialog locationDialog = dataEntry.getLocationDialog();

        locationDialog.locationInput().sendKeys("Mwenu Ditu");

        try {
            locationDialog.addNew();
            locationDialog.dragMarker(5, 5);
        } catch (TimeoutException e) {
            // sometimes test fails because it can't find marker, however tried it manually and it always appears
            // retry one time
            locationDialog.addNew();
            locationDialog.dragMarker(5, 5);
        }

        GxtFormPanel.GxtField latitude = locationDialog.getFormPanel().findFieldByLabel(I18N.CONSTANTS.latitude());
        latitude.assertValid();
    }
    
}
