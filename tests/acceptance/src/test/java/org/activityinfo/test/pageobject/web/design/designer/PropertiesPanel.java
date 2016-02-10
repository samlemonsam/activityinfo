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

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.driver.FieldValue;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.api.XPathBuilder;
import org.activityinfo.test.pageobject.bootstrap.BsFormPanel;
import org.activityinfo.test.pageobject.bootstrap.BsModal;

import java.util.List;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withClass;

/**
 * @author yuriyz on 06/15/2015.
 */
public class PropertiesPanel {

    private final FluentElement container;

    public PropertiesPanel(FluentElement container) {
        this.container = container;
    }

    public BsFormPanel form() {
        return new BsFormPanel(container.find().div(withClass("panel-body")).first());
    }

    public FluentElement getContainer() {
        return container;
    }

    public void setValues(List<FieldValue> values) {
        for (FieldValue value : values) {
            switch(value.getField()) {
                case "code":
                    setProperty(I18N.CONSTANTS.codeFieldLabel(), value.getValue());
                    break;
                case "label":
                    setProperty(I18N.CONSTANTS.labelFieldLabel(), value.getValue());
                    break;
                case "description":
                    setProperty(I18N.CONSTANTS.description(), value.getValue());
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown designer field property: " + value.getField());
            }
        }
    }

    public void setProperty(String fieldLabel, String value) {
        form().findFieldByLabel(fieldLabel).fill(value);
    }

    public void selectProperty(String fieldLabel, String value) {
        form().findFieldByLabel(fieldLabel).select(value);
    }

    public void setLabel(String label) {
        setProperty(I18N.CONSTANTS.labelFieldLabel(), label);
    }

    public RelevanceDialog relevanceDialog() {
        form().findFieldByLabel(I18N.CONSTANTS.relevance()).select(I18N.CONSTANTS.relevanceEnabledIf());
        form().getForm().find().button(XPathBuilder.withText(I18N.CONSTANTS.defineRelevanceLogic())).clickWhenReady();

        return new RelevanceDialog(BsModal.waitForModal(container));
    }
}
