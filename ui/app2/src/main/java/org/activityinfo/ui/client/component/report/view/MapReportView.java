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
package org.activityinfo.ui.client.component.report.view;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.event.dom.client.ClickHandler;
import org.activityinfo.legacy.shared.reports.model.MapReportElement;
import org.activityinfo.ui.client.component.report.editor.map.LeafletMap;
import org.activityinfo.ui.client.component.report.editor.map.symbols.LeafletReportOverlays;
import org.activityinfo.ui.client.util.LeafletUtil;
import org.discotools.gwt.leaflet.client.LeafletResourceInjector;
import org.discotools.gwt.leaflet.client.map.MapOptions;

public class MapReportView extends ContentPanel implements ReportView<MapReportElement> {

    private LeafletMap map;
    private LeafletReportOverlays overlays;
    private MapReportElement element;

    public MapReportView() {
        LeafletResourceInjector.ensureInjected();

        setLayout(new FitLayout());
        setHeaderVisible(false);
    }

    @Override
    public void loading() {
        if(isRendered()) {
            el().mask();
        }
    }

    @Override
    public void onFailure(Throwable caught, ClickHandler retryCallback) {
        ReportViewRetrier.onFailure(this, caught, retryCallback);
        map = null;
    }


    @Override
    public void show(MapReportElement element) {
        if(isRendered()) {
            el().unmask();
        }
        this.element = element;
        addContent();
    }

    private void addContent() {
        if (map == null) {
            MapOptions options = new MapOptions();
            options.setZoom(element.getContent().getZoomLevel());
            options.setCenter(LeafletUtil.to(element.getContent().getCenter()));
            options.setProperty("zoomControl", false);
            options.setProperty("attributionControl", false);

            map = new LeafletMap(options);
            add(map);
            layout();
        }
        if (map.isRendered()) {
            syncContent();
        } else {
            map.addListener(Events.Render, new Listener<BaseEvent>() {

                @Override
                public void handleEvent(BaseEvent be) {
                    syncContent();
                }
            });
        }
    }

    private void syncContent() {
        if (overlays == null) {
            overlays = new LeafletReportOverlays(map.getMap());
        }
        overlays.syncWith(element);
    }

    @Override
    public void afterRender() {
        super.afterRender();

        if (element != null) {
            addContent();
        }
    }

    @Override
    public Component asComponent() {
        return this;
    }

}
