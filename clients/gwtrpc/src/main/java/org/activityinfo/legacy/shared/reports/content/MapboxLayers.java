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
package org.activityinfo.legacy.shared.reports.content;

import org.activityinfo.legacy.shared.model.BaseMap;
import org.activityinfo.legacy.shared.model.TileBaseMap;

public class MapboxLayers {

    public static final String MAPBOX_STREETS = "https://{s}.tiles.mapbox.com/v3/activityinfo.gc3n5efh/{z}/{x}/{y}.png";

    public static final String MAPBOX_TERRAIN = "https://{s}.tiles.mapbox.com/v3/activityinfo.gcg3g01h/{z}/{x}/{y}.png";

    public static final String MAPBOX_SATELLITE = "https://{s}.tiles.mapbox.com/v3/activityinfo.gcg3l5eb/{z}/{x}/{y}" +
                                                  ".png";

    public static final String MAPBOX_HYBRID = "https://{s}.tiles.mapbox.com/v3/activityinfo.gcg4ei82/{z}/{x}/{y}.png";

    public static TileBaseMap toTileBaseMap(BaseMap baseMap) {
        if (baseMap instanceof TileBaseMap) {
            return (TileBaseMap) baseMap;
        } else {
            String url;
            if (baseMap.equals(GoogleBaseMap.ROADMAP)) {
                url = MAPBOX_STREETS;
            } else if (baseMap.equals(GoogleBaseMap.TERRAIN)) {
                url = MAPBOX_TERRAIN;
            } else if (baseMap.equals(GoogleBaseMap.SATELLITE)) {
                url = MAPBOX_SATELLITE;
            } else if (baseMap.equals(GoogleBaseMap.HYBRID)) {
                url = MAPBOX_HYBRID;
            } else {
                url = MAPBOX_STREETS;
            }
            TileBaseMap tileBaseMap = new TileBaseMap();
            tileBaseMap.setId(url);
            tileBaseMap.setMinZoom(2);
            tileBaseMap.setMaxZoom(18);
            tileBaseMap.setTileUrlPattern(url);
            return tileBaseMap;
        }
    }
}
