package org.activityinfo.test.pageobject.bootstrap;
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

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.NarrativeType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.time.LocalDateType;
import org.activityinfo.test.driver.FieldValue;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.api.XPathBuilder;
import org.activityinfo.test.pageobject.web.components.ModalDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.List;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withClass;

/**
 * @author yuriyz on 05/12/2015.
 */
public class BsModal extends ModalDialog {

    public static final String CLASS_NAME = "modal-dialog";

    private FluentElement windowElement;

    public BsModal(FluentElement windowElement) {
        this.windowElement = windowElement;
    }

    public static BsModal find(FluentElement parent) {
        FluentElement fluentElement = parent.waitFor(By.className(CLASS_NAME));
        if (fluentElement.isDisplayed()) {
            return new BsModal(fluentElement);
        }

        // there can be multiple modal dialogs attached, we have to find the displayed one.
        for(FluentElement element : parent.findElements(By.className(CLASS_NAME)).list()) {
            if (element.isDisplayed()) {
                return new BsModal(element);
            }
        }

        throw new AssertionError("Failed to find bootstrap modal dialog.");
    }

    public String getTitle() {
        return windowElement.find().div(withClass("modal-header")).first().text();
    }

    @Override
    public BsFormPanel form() {
        return new BsFormPanel(windowElement.find().div(withClass("modal-body")).first());
    }

    public BsModal fill(List<FieldValue> fieldValues) {
        BsFormPanel form = form();
        for (FieldValue value : fieldValues) {
            fill(form, value);
        }
        return this;
    }

    private void fill(BsFormPanel form, FieldValue value) {
        BsFormPanel.BsField item = form.findFieldByLabel(value.getField());

        // fill by control type
        if ("radio".equalsIgnoreCase(value.getControlType())) {
            item.select(value.getValue());
            return;
        }

        // fill by type
        Optional<? extends FieldTypeClass> type = value.getType();
        if (type == null || !type.isPresent() || type.get() == TextType.TYPE_CLASS || type.get() == NarrativeType.TYPE_CLASS) {
            item.fill(value.getValue());
        } else {
            if (type.get() == LocalDateType.TYPE_CLASS) {
                item.fill(org.joda.time.LocalDate.parse(value.getValue()));
            } else if (type.get() == EnumType.TYPE_CLASS) {
                item.select(value.getValue());
            }
        }
    }

    private FluentElement buttonsContainer() {
        return windowElement.find().div(withClass("modal-footer")).first();
    }

    public BsModal click(String buttonName) {
        click(buttonName, "", false);
        return this;
    }

    public void click(String buttonName, final String expectedTitle) {
        click(buttonName, expectedTitle, true);
    }

    public void click(String buttonName, final String expectedTitle, boolean waitOnTitle) {
        buttonsContainer().find().button(XPathBuilder.withText(buttonName)).clickWhenReady();
        if (waitOnTitle) {
            windowElement.waitUntil(new Predicate<WebDriver>() {
                @Override
                public boolean apply(WebDriver input) {

                    if (!windowElement.isDisplayed()) {  // is the box closed?
                        return false;
                    }

                    if (getTitle().contains(expectedTitle)) {
                        return true;
                    }

                    // check for an error ?

                    return false;
                }
            });
        }
    }

    @Override
    public void accept() {
        click(I18N.CONSTANTS.ok());
        waitUntilClosed();
    }

    public boolean isClosed() {
        return !windowElement.isDisplayed();
    }

    public FluentElement getWindowElement() {
        return windowElement;
    }

    public void waitUntilClosed() {
        windowElement.waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return !windowElement.isDisplayed();
            }
        });
    }
}
