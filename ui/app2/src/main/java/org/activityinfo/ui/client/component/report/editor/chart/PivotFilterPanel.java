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

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.reports.model.PivotReportElement;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.component.filter.*;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.dispatch.ResourceLocator;
import org.activityinfo.ui.client.page.report.HasReportElement;
import org.activityinfo.ui.client.page.report.ReportChangeHandler;
import org.activityinfo.ui.client.page.report.ReportEventBus;

public class PivotFilterPanel extends ContentPanel implements HasReportElement<PivotReportElement> {
    private final FilterPanelSet panelSet;
    private final ReportEventBus reportEventBus;

    private PivotReportElement model;

    public PivotFilterPanel(EventBus eventBus, Dispatcher dispatcher, ResourceLocator locator) {
        this.reportEventBus = new ReportEventBus(eventBus, this);

        setLayout(new AccordionLayout());
        setHeadingText(I18N.CONSTANTS.filter());

        IndicatorFilterPanel indicatorPanel = new IndicatorFilterPanel(dispatcher, locator);
        indicatorPanel.setHeaderVisible(true);
        add(indicatorPanel);

        AdminFilterPanel adminFilterPanel = new AdminFilterPanel(dispatcher);
        add(adminFilterPanel);

        DateRangePanel startDateFilterPanel = new DateRangePanel(DateRangePanel.DateType.START);
        add(startDateFilterPanel);
        
        DateRangePanel endDateFilterPanel = new DateRangePanel(DateRangePanel.DateType.END);
        add(endDateFilterPanel);

        PartnerFilterPanel partnerFilterPanel = new PartnerFilterPanel(dispatcher);
        add(partnerFilterPanel);

        AttributeFilterPanel attributePanel = new AttributeFilterPanel(dispatcher);
        add(attributePanel);

        LocationFilterPanel locationFilterPanel = new LocationFilterPanel(dispatcher);
        add(locationFilterPanel);

        panelSet = new FilterPanelSet(indicatorPanel,
                adminFilterPanel,
                startDateFilterPanel,
                endDateFilterPanel,
                partnerFilterPanel,
                attributePanel,
                locationFilterPanel);

        // if a nested filterpanel changed the filter
        panelSet.addValueChangeHandler(new ValueChangeHandler<Filter>() {
            @Override
            public void onValueChange(ValueChangeEvent<Filter> event) {
                model.setFilter(event.getValue());
                // notify other filterpanels
                panelSet.setValue(model.getFilter(), false);
                // notify other components
                PivotFilterPanel.this.reportEventBus.fireChange();
            }
        });

        // if another component changed the model
        this.reportEventBus.listen(new ReportChangeHandler() {
            @Override
            public void onChanged() {
                panelSet.setValue(model.getFilter(), false);
            }
        });
    }

    @Override
    public void bind(PivotReportElement model) {
        this.model = model;
        panelSet.setValue(model.getFilter(), false);
    }

    @Override
    public PivotReportElement getModel() {
        return model;
    }

    public void applyBaseFilter(Filter filter) {
        panelSet.applyBaseFilter(filter);
    }

    @Override
    public void disconnect() {
        reportEventBus.disconnect();
    }
}
