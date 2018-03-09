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
package org.activityinfo.test.pageobject.web.entry;

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.gxt.GxtFormPanel;
import org.activityinfo.test.pageobject.gxt.GxtModal;

import static java.lang.String.format;
import static org.activityinfo.test.pageobject.api.XPathBuilder.*;

public class LocationDialog {

    private GxtModal dialog;
    private final GxtFormPanel formPanel;

    public LocationDialog(GxtModal dialog) {
        this.dialog = dialog;

        formPanel = new GxtFormPanel(dialog.getWindowElement()
                .find().div(withClass("x-component"), withClass("x-form-label-left")).first());

    }

    public FluentElement locationInput() {
        return dialog.getWindowElement().find().input(
                withClass("x-form-field"),
                withClass("x-form-text"),
                withoutClass("x-triggerfield-noedit"))
                .first();
    }

    public GxtFormPanel getFormPanel() {
        return formPanel;
    }

    public static LocationDialog get(FluentElement root) {
        GxtModal modal = new GxtModal(root);
        if(!modal.getTitle().equals(I18N.CONSTANTS.chooseLocation())) {
            throw new AssertionError(format("Expected dialog with title '%s', found '%s'",
                    I18N.CONSTANTS.chooseLocation(), modal.getTitle()));
        }

        return new LocationDialog(modal);
    }

    public GxtFormPanel.GxtField latitudeField() {
        return formPanel.findFieldByLabel(I18N.CONSTANTS.latitude());
    }

    public GxtFormPanel.GxtField longitudeField() {
        return formPanel.findFieldByLabel(I18N.CONSTANTS.longitude());
    }


    public void addNew() {
        dialog.getWindowElement().find().button(withText(I18N.CONSTANTS.newLocation()))
                .waitForFirst().clickWhenReady();
    }

    public void dragMarker(int pixelsToLeft, int pixelsToRight) {
        FluentElement marker = dialog.getWindowElement().find()
                .div(withClass("leaflet-marker-pane"))
                .div(withClass("leaflet-marker-icon"))
                .waitForFirst();
        
        marker.dragAndDropBy(pixelsToLeft, pixelsToRight);

    }

    public void close() {
        dialog.closeByWindowHeaderButton();
    }
}
