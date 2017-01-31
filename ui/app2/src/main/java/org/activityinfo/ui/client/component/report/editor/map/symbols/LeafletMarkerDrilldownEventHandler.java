package org.activityinfo.ui.client.component.report.editor.map.symbols;
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

import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.reports.content.MapMarker;
import org.activityinfo.ui.client.component.report.view.DrillDownEditor;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.discotools.gwt.leaflet.client.events.Event;
import org.discotools.gwt.leaflet.client.events.handler.EventHandler;
import org.discotools.gwt.leaflet.client.jsobject.JSObject;

/**
 * @author yuriyz on 4/23/14.
 */
public class LeafletMarkerDrilldownEventHandler implements EventHandler<Event> {

    private final DrillDownEditor drillDownEditor;

    public LeafletMarkerDrilldownEventHandler(Dispatcher dispatcher) {
        this.drillDownEditor = new DrillDownEditor(dispatcher);
    }

    @Override
    public void handle(Event event) {
        final JSObject targetOptions = event.getTarget().getProperty("options");
        MapMarker markerModel = LeafletMarkerFactory.getModel(targetOptions);

        final Filter effectiveFilter = new Filter();
        effectiveFilter.addRestriction(DimensionType.Site, markerModel.getSiteIds());
        effectiveFilter.addRestriction(DimensionType.Indicator, markerModel.getIndicatorIds());

        drillDownEditor.drillDown(effectiveFilter);
    }

    public void setPosition(int bottomX, int bottomY) {
        drillDownEditor.setPosition(bottomX - DrillDownEditor.WIDTH, bottomY - DrillDownEditor.HEIGHT);
    }
}
