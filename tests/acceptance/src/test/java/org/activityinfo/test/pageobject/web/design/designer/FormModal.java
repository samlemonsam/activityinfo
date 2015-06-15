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

import com.google.common.base.Function;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.bootstrap.BsModal;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withClass;

/**
 * @author yuriyz on 06/15/2015.
 */
public class FormModal {

    public static BsModal find(FluentElement container) {
        FluentElement dialogElement = container.waitFor(new Function<WebDriver, FluentElement>() {
            @Override
            public FluentElement apply(WebDriver driver) {
                List<WebElement> elements = driver.findElements(By.tagName("label"));
                for (WebElement element : elements) {
                    if (element.getText().contains("Start Date")) {
                        return new FluentElement(driver, element).find().ancestor().div(withClass(BsModal.CLASS_NAME)).first();
                    }
                }

                return null;
            }
        });
        return new BsModal(dialogElement);
    }
}
