package org.activityinfo.test.ui;

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.driver.AliasTable;
import org.activityinfo.test.driver.UiApplicationDriver;
import org.activityinfo.test.pageobject.web.ApplicationPage;
import org.activityinfo.test.pageobject.web.design.designer.FormDesignerPage;
import org.junit.Test;

import javax.inject.Inject;
import java.util.List;

import static org.activityinfo.test.driver.Property.name;
import static org.activityinfo.test.driver.Property.property;

/**
 * Randomly manipulates Design form to try generating errors
 */
public class FormDesignUiTest {

    public static final String DATABASE = "My Database";
    @Inject
    private UiApplicationDriver driver;
    
    @Inject
    private AliasTable aliasTable;
    
    @Test
    public void addingFields() throws Exception {
        driver.login();
        driver.setup().createDatabase(name(DATABASE));
        driver.setup().createForm(name("Form"), property("database", DATABASE));
        ApplicationPage applicationPage = driver.getApplicationPage();
        FormDesignerPage designer = applicationPage.navigateToFormDesigner(
                aliasTable.getAlias(DATABASE),
                aliasTable.getAlias("Form"));

        List<String> types = designer.fields().getFieldTypes();

        if(types.isEmpty()) {
            throw new AssertionError("Could not find any field types");
        }

        for (String type : types) {
            designer.fields().add(type);
            designer.save();
        }
        
        designer.fields().add(I18N.CONSTANTS.fieldTypeCalculated());
        
        


    }
}
