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

import java.util.List;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withClass;
import static org.activityinfo.test.pageobject.api.XPathBuilder.withText;

/**
 * @author yuriyz on 06/12/2015.
 */
public class FormDesignerPage {

    public static final String DROP_TARGET_CLASS = "dragdrop-dropTarget";

    private final FluentElement container;
    private DropPanel rootDropPanel;

    public FormDesignerPage(FluentElement container) {
        this.container = container;
        this.rootDropPanel = new DropPanel(container.find().div(
                withClass(FormDesignerPage.DROP_TARGET_CLASS), withClass("main-panel")).first());
    }

    public DropPanel rootDropTarget() {
        return rootDropPanel;
    }

    public DropPanel dropPanel(String containerLabel) {
        if ("root".equalsIgnoreCase(containerLabel)) {
            return rootDropPanel;
        } else {
            return subTarget(containerLabel);
        }
    }

    private DropPanel subTarget(String label) {
        return new DropPanel(container.find().h3(withText(label)).
                ancestor().div(withClass("dragdrop-draggable")).descendants().div(
                withClass(FormDesignerPage.DROP_TARGET_CLASS), withClass("section-widget-container")).first());
    }

    private List<FluentElement> dropTargets() {
        return container.find().div(withClass(FormDesignerPage.DROP_TARGET_CLASS)).asList().list();
    }

    public PropertiesPanel fieldProperties() {
        selectTab("Field");
        List<FluentElement> panels = container.find().div(withClass("panel")).waitForList().list();
        return new PropertiesPanel(panels.get(0));

    }

    public PropertiesPanel containerProperties() {
        selectTab("Container");
        List<FluentElement> panels = container.find().div(withClass("panel")).waitForList().list();
        return new PropertiesPanel(panels.get(1));
    }

    private void selectTab(String tabText) {
        container.find().b(withText(tabText)).first().clickWhenReady();
    }

    public FieldPalette fields() {
        return new FieldPalette(container.find()
                .h4(withText(I18N.CONSTANTS.fields()))
                .ancestor().div(withClass("panel"))
                .waitForFirst());
    }

    public DesignerField selectFieldByLabel(String label) {
        DesignerField designerField = rootDropTarget().fieldByLabel(label);
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

        fieldProperties().relevanceDialog().set(dataTable, alias);
    }
}
