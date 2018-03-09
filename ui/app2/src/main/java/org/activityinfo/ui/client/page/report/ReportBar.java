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
package org.activityinfo.ui.client.page.report;

import com.extjs.gxt.ui.client.event.EditorEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.page.common.toolbar.ExportMenuButton;
import org.activityinfo.ui.client.page.common.toolbar.SaveMenuButton;
import org.activityinfo.ui.client.page.report.resources.ReportResources;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

public class ReportBar extends LayoutContainer {

    private ReportTitleWidget titleWidget;
    private SaveMenuButton saveButton;
    private ToggleButton dashboardButton;
    private ExportMenuButton exportButton;
    private Button shareButton;
    private Button switchViewButton;

    public ReportBar() {
        setStyleName(ReportResources.INSTANCE.style().bar());

        HBoxLayout layout = new HBoxLayout();
        layout.setHBoxLayoutAlign(HBoxLayoutAlign.MIDDLE);
        layout.setPadding(new Padding(5));

        setLayout(layout);
        setMonitorWindowResize(true);

        addTitle();

        switchViewButton = new Button(I18N.CONSTANTS.switchToPageView(), IconImageBundle.ICONS.page());
        add(switchViewButton);

        dashboardButton = new ToggleButton(I18N.CONSTANTS.pinToDashboard(), IconImageBundle.ICONS.star());
        add(dashboardButton);

        shareButton = new Button(I18N.CONSTANTS.share(), IconImageBundle.ICONS.group());
        add(shareButton);

        exportButton = new ExportMenuButton();
        add(exportButton);

        saveButton = new SaveMenuButton();
        add(saveButton);

    }

    private void addTitle() {

        titleWidget = new ReportTitleWidget();

        HBoxLayoutData titleLayout = new HBoxLayoutData(0, 0, 0, 7);
        titleLayout.setFlex(1);

        add(titleWidget, titleLayout);
    }

    public void addTitleEditCompleteListener(Listener<EditorEvent> listener) {
        titleWidget.addEditCompleteListener(listener);
    }

    public void setReportTitle(String value) {
        titleWidget.setText(value);
    }

    public SaveMenuButton getSaveButton() {
        return saveButton;
    }

    public ToggleButton getDashboardButton() {
        return dashboardButton;
    }

    public ExportMenuButton getExportButton() {
        return exportButton;
    }

    public Button getShareButton() {
        return shareButton;
    }

    public Button getSwitchViewButton() {
        return switchViewButton;
    }
}
