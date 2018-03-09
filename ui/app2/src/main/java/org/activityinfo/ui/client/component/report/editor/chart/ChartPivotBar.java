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
package org.activityinfo.ui.client.component.report.editor.chart;

import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import org.activityinfo.legacy.shared.reports.model.PivotChartReportElement;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.page.report.HasReportElement;

public class ChartPivotBar extends ToolBar implements HasReportElement<PivotChartReportElement> {

    private DimensionComboBoxSet comboBoxes;

    public ChartPivotBar(EventBus eventBus, Dispatcher dispatcher) {
        comboBoxes = new DimensionComboBoxSet(eventBus, dispatcher);

        add(comboBoxes.getCategoryLabel());
        add(comboBoxes.getCategoryCombo());

        add(new FillToolItem());

        add(comboBoxes.getSeriesLabel());
        add(comboBoxes.getSeriesCombo());
    }

    @Override
    public void bind(PivotChartReportElement model) {
        this.comboBoxes.bind(model);
    }

    @Override
    public PivotChartReportElement getModel() {
        return this.comboBoxes.getModel();
    }

    @Override
    public void disconnect() {
        this.comboBoxes.disconnect();
    }

}
