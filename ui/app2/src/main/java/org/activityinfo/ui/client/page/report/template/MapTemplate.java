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
package org.activityinfo.ui.client.page.report.template;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.reports.model.MapReportElement;
import org.activityinfo.legacy.shared.reports.model.ReportElement;
import org.activityinfo.ui.client.component.report.editor.map.NewLayerWizard;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.dispatch.ResourceLocator;
import org.activityinfo.ui.client.widget.wizard.WizardCallback;
import org.activityinfo.ui.client.widget.wizard.WizardDialog;

public class MapTemplate extends ReportElementTemplate {

    private ResourceLocator locator;

    public MapTemplate(Dispatcher dispatcher, ResourceLocator locator) {
        super(dispatcher);
        this.locator = locator;
        setName(I18N.CONSTANTS.maps());
        setDescription(I18N.CONSTANTS.mapsDescription());
        setImagePath("map.png");
    }

    @Override
    public void createElement(final AsyncCallback<ReportElement> callback) {

        final NewLayerWizard wizard = new NewLayerWizard(dispatcher, locator);
        WizardDialog dialog = new WizardDialog(wizard);
        dialog.setHeadingText(I18N.CONSTANTS.newMap());
        dialog.show(new WizardCallback() {

            @Override
            public void onFinished() {
                MapReportElement map = new MapReportElement();
                map.addLayer(wizard.createLayer());

                callback.onSuccess(map);
            }
        });
    }
}
