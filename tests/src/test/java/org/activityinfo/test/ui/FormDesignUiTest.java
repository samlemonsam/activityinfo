/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
        driver.setup().createForm(name("Form"), property("database", DATABASE), property("classicView", false));
        ApplicationPage applicationPage = driver.getApplicationPage();
        FormDesignerPage designer = applicationPage.navigateToFormDesigner(
                aliasTable.getAlias(DATABASE),
                aliasTable.getAlias("Form"));

        List<String> types = designer.fields().getFieldTypes();

        if(types.isEmpty()) {
            throw new AssertionError("Could not find any field types");
        }

        for (String type : types) {
            designer.fields().dropNewField(type);
            designer.save();
        }
        
        designer.fields().dropNewField(I18N.CONSTANTS.fieldTypeCalculated());
    }
}
