package org.activityinfo.test.pageobject.web.design.designer;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.base.Predicate;
import cucumber.api.DataTable;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.driver.AliasTable;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.openqa.selenium.WebDriver;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withClass;
import static org.activityinfo.test.pageobject.api.XPathBuilder.withText;

/**
 * @author yuriyz on 06/12/2015.
 */
public class FormDesignerPage {

    public static final String DROP_TARGET_CLASS = "dragdrop-dropTarget";

    private final FluentElement container;

    public FormDesignerPage(FluentElement container) {
        this.container = container;
    }

    public DropPanel dropTarget() {
        return new DropPanel(container.find().div(withClass(FormDesignerPage.DROP_TARGET_CLASS)).first());
    }

    public PropertiesPanel properties() {
        return new PropertiesPanel(container.find().div(withClass("panel-heading")).
                ancestor().div(withClass("panel")).first());
    }
    
    public FieldPalette fields() {
        return new FieldPalette(container.find()
                .h4(withText(I18N.CONSTANTS.fields()))
                .ancestor().div(withClass("panel"))
                .waitForFirst());
    }

    public DesignerField selectFieldByLabel(String label) {
        DesignerField designerField = dropTarget().fieldByLabel(label);
        designerField.element().clickWhenReady();
        return designerField;
    }

    public void save() {
        container.find().button(withText(I18N.CONSTANTS.save())).clickWhenReady();
        container.waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                if (container.find().div(withText(I18N.CONSTANTS.saved())).exists()) {
                    return true;
                }
                if (container.find().div(withText(I18N.CONSTANTS.failedToSaveClass())).exists()) {
                    throw new AssertionError("Save failed");
                }
                return false;
            }
        });
    }

    public void setRelevance(String fieldLabel, DataTable dataTable, AliasTable alias) {
        selectFieldByLabel(fieldLabel);

        properties().relevanceDialog().set(dataTable, alias);
    }
}
