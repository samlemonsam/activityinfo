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
package org.activityinfo.ui.client.component.report.editor.map.symbols;

import org.activityinfo.legacy.shared.reports.content.AdminMarker;
import org.activityinfo.legacy.shared.reports.content.AdminOverlay;
import org.activityinfo.ui.client.util.LeafletUtil;
import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.discotools.gwt.leaflet.client.layers.others.GeoJSONFeatures;

public class AdminChloroplethFeatures extends GeoJSONFeatures {

    private final AdminOverlay overlay;


    public AdminChloroplethFeatures(AdminOverlay overlay) {
        super();
        this.overlay = overlay;
    }

    @Override
    public JSObject pointToLayer(JSObject feature, JSObject latlng) {
        return null;
    }

    @Override
    public JSObject onEachFeature(JSObject feature, JSObject layer) {
        return feature;
    }

    @Override
    public JSObject style(JSObject feature) {
        int adminEntityId = feature.getPropertyAsInt("id");
        AdminMarker polygon = overlay.getPolygon(adminEntityId);

        JSObject style = JSObject.createJSObject();
        style.setProperty("fillColor", LeafletUtil.color(polygon.getColor()));
        style.setProperty("fillOpacity", 0.5);
        style.setProperty("stroke", true);
        style.setProperty("weight", 2);
        style.setProperty("color", LeafletUtil.color(overlay.getOutlineColor()));
        return style;
    }

    @Override
    public boolean filter(JSObject feature, JSObject layer) {
        return true;
    }

}
