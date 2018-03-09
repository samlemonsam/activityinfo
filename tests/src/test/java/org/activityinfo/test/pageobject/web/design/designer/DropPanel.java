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

import java.util.List;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withClass;

/**
 * @author yuriyz on 06/15/2015.
 */
public class DropPanel {

    private final FluentElement container;

    public DropPanel(FluentElement container) {
        this.container = container;
    }

    public DesignerField fieldByLabel(String label) {
        for (DesignerField field : fields()) {
            String fieldLabel = field.getLabel();
            if (fieldLabel.equals(label)) {
                return field;
            }
        }
        throw new AssertionError("Failed to find designer field with label: " + label);
    }

    public DropPanel dragAndDrop(String fieldLabel, int positionToDrop) {
        return dragAndDrop(fieldByLabel(fieldLabel), positionToDrop);
    }

    public DropPanel dragAndDrop(DesignerField field, int positionToDrop) {
        DesignerField fieldAt = fields().get(positionToDrop);
        field.draggable().dragAndDrop(fieldAt.draggable());
        return this;
    }

    public DropPanel dragAndDrop(DropLabel label) {
        label.getElement().dragAndDrop(container);
        return this;
    }

    public int fieldPosition(String fieldLabel) {
        List<DesignerField> fields = fields();
        for (DesignerField field: Lists.newArrayList(fields)) {
            if (field.getLabel().equals(fieldLabel)) {
                return fields.indexOf(field);
            }
        }
        throw new AssertionError("Failed to identify index of field with lable: " + fieldLabel);
    }

    public List<DesignerField> fields() {
        List<FluentElement> elements = container.find().div(withClass("widget-container")).asList().list();
        List<DesignerField> fields = Lists.newArrayList();
        for (FluentElement element : elements) {
            fields.add(new DesignerField(element));
        }
        return fields;
    }

    public FluentElement getContainer() {
        return container;
    }
}
