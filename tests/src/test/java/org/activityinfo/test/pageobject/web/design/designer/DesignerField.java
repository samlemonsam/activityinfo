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

import com.google.common.base.Optional;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.driver.ControlType;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.bootstrap.BsModal;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withClass;
import static org.activityinfo.test.pageobject.api.XPathBuilder.withText;

/**
 * @author yuriyz on 06/12/2015.
 */
public class DesignerField {

    public static final String MANDATORY_MARKER = "*";

    private final FluentElement element;

    public DesignerField(FluentElement element) {
        this.element = element;
    }

    public String getLabel() {
        String label = labelWithMandatoryMarker().trim();
        if (label.endsWith(MANDATORY_MARKER)) {
            // cut off mandatory marker
            return label.substring(0, label.length() - 1).trim();
        }
        return label;
    }

    public boolean isMandatory() {
        return labelWithMandatoryMarker().trim().endsWith(MANDATORY_MARKER);
    }

    private String labelWithMandatoryMarker() {
        Optional<FluentElement> labelElement = element.find().div().div(withClass("h5")).firstIfPresent();
        if (labelElement.isPresent()) {
            FluentElement header = labelElement.get();
            String label = labelElement.get().text();
            Optional<FluentElement> code = header.find().span(withClass("small")).firstIfPresent();
            if(code.isPresent()) {
                label = label.substring(code.get().text().length()+1);
            }
            return label;
        }
        return "";
    }

    public boolean isDeletable() {
        BsModal bsModal = clickDelete();
        if (bsModal.getTitle().contains(I18N.CONSTANTS.warning())) {
            bsModal.click(I18N.CONSTANTS.ok());
            return false;
        }
        if (bsModal.getTitle().contains(I18N.CONSTANTS.confirmDeletion())) {
            bsModal.click(I18N.CONSTANTS.cancel());
            return true;
        }

        throw new AssertionError("Failed to identify deletable state.");
    }

    public DesignerField delete() {
        BsModal bsModal = clickDelete();
        bsModal.click(I18N.CONSTANTS.delete());
        return this;
    }

    public FluentElement draggable() {
        return element.find().div(withClass("dragdrop-handle"), withText("|||")).first();
    }

    private BsModal clickDelete() {
        element.find().button(withClass("btn-link"), withText("x")).first().clickWhenReady();
        return BsModal.find(element.root());
    }

    public FluentElement element() {
        return element;
    }

    public ControlType getControlType() {
        throw new RuntimeException("Unable to find out control type");
    }
}
