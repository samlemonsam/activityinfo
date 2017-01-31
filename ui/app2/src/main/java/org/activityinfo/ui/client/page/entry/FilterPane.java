package org.activityinfo.ui.client.page.entry;

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

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.component.filter.*;
import org.activityinfo.ui.client.dispatch.Dispatcher;

public class FilterPane extends ContentPanel {

    private final FilterPanelSet filterPanelSet;

    public FilterPane(Dispatcher dispatcher) {
        setHeadingText(I18N.CONSTANTS.filter());
        setLayout(new AccordionLayout());

        ActivityFilterPanel activityFilterPanel = new ActivityFilterPanel(dispatcher);
        AdminFilterPanel adminFilterPanel = new AdminFilterPanel(dispatcher);
        DateRangePanel startDatePanel = new DateRangePanel(DateRangePanel.DateType.START);
        DateRangePanel endDatePanel = new DateRangePanel(DateRangePanel.DateType.END);
        PartnerFilterPanel partnerPanel = new PartnerFilterPanel(dispatcher);
        AttributeFilterPanel attributePanel = new AttributeFilterPanel(dispatcher);
        LocationFilterPanel locationFilterPanel = new LocationFilterPanel(dispatcher);

        add(activityFilterPanel);
        add(adminFilterPanel);
        add(endDatePanel);
        add(startDatePanel);
        add(partnerPanel);
        add(attributePanel);
        add(locationFilterPanel);

        filterPanelSet = new FilterPanelSet(activityFilterPanel,
                adminFilterPanel,
                endDatePanel,
                startDatePanel,
                partnerPanel,
                attributePanel,
                locationFilterPanel);
    }

    public FilterPanelSet getSet() {
        return filterPanelSet;
    }

}
