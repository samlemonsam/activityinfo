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
package org.activityinfo.test.pageobject.web.design.designer;

import com.google.common.collect.Lists;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.api.FluentElements;

import java.util.List;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withClass;
import static org.activityinfo.test.pageobject.api.XPathBuilder.withText;


public class FieldPalette {
    private FluentElement panel;

    public FieldPalette(FluentElement panel) {
        this.panel = panel;
    }

    public void dropNewField(String name) {
        dropLabel(name).clickWhenReady();
    }

    public DropLabel dropLabel(String name) {
        return new DropLabel(panel.find().div(withText(name)).waitForFirst(), name);
    }
    
    public List<String> getFieldTypes() {
        FluentElements elements = panel.find().div(withClass("btn")).asList();
        List<String> types = Lists.newArrayList();
        for (FluentElement element : elements) {
            types.add(element.text());
        }
        return types;
    }
}
