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
package org.activityinfo.test.pageobject.gxt;

import com.google.common.base.Optional;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.openqa.selenium.By;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withClass;

public class GxtMessageBox {

    private FluentElement element;

    public GxtMessageBox(FluentElement element) {
        this.element = element;
    }

    public static Optional<GxtMessageBox> get(FluentElement element) {
        Optional<FluentElement> messageBox = element.root().find()
                .div(withClass("ext-mb-icon"))
                .ancestor()
                .div(withClass("x-window")).firstIfPresent();
        if(messageBox.isPresent()) {
            return Optional.of(new GxtMessageBox(messageBox.get()));
        } else {
            return Optional.absent();
        }
    }
    
    public boolean isWarning() {
        return element.exists(By.className("ext-mb-warning"));
    }
    
    public String getMessage() {
        return element.findElement(By.className("ext-mb-content")).text();
    }
    
}
