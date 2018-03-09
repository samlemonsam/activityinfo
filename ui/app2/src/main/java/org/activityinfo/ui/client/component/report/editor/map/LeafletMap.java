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
package org.activityinfo.ui.client.component.report.editor.map;

import com.extjs.gxt.ui.client.widget.Html;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.activityinfo.model.type.geo.Extents;
import org.activityinfo.ui.client.util.LeafletUtil;
import org.discotools.gwt.leaflet.client.LeafletResourceInjector;
import org.discotools.gwt.leaflet.client.map.Map;
import org.discotools.gwt.leaflet.client.map.MapOptions;

public class LeafletMap extends Html {

    private MapOptions options;
    private Map map;

    public LeafletMap(MapOptions options) {
        this.options = options;
        LeafletResourceInjector.ensureInjected();

        setStyleName("gwt-Map");
        setHtml(SafeHtmlUtils.fromSafeConstant("<div style=\"width:100%; height: 100%; position: relative;\"></div>"));
    }

    @Override
    protected void afterRender() {
        super.afterRender();
        map = new Map(getElement().getElementsByTagName("div").getItem(0), options);
    }

    @Override
    protected void onResize(int width, int height) {
        super.onResize(width, height);
        map.invalidateSize(false);
    }

    public Map getMap() {
        return map;
    }

    public void fitBounds(Extents extents) {
        map.fitBounds(LeafletUtil.newLatLngBounds(extents));
    }
}
