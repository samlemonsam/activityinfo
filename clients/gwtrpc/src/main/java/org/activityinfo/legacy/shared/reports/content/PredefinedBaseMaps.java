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

import java.util.HashMap;
import java.util.Map;

public final class PredefinedBaseMaps {

    private PredefinedBaseMaps() {
    }

    private static Map<String, BaseMap> maps;

    static {
        maps = new HashMap<String, BaseMap>();
        maps.put(GoogleBaseMap.ROADMAP.getId(), GoogleBaseMap.ROADMAP);
        maps.put(GoogleBaseMap.SATELLITE.getId(), GoogleBaseMap.SATELLITE);
        maps.put(GoogleBaseMap.HYBRID.getId(), GoogleBaseMap.HYBRID);
        maps.put(GoogleBaseMap.TERRAIN.getId(), GoogleBaseMap.TERRAIN);
    }

    public static boolean isPredefinedMap(String id) {
        return maps.containsKey(id);
    }

    /**
     * @param id
     * @return a reference to the predefined base map identified by {@code id},
     * or {@code null} if there is no predefined basemap with this id.
     */
    public static BaseMap forId(String id) {
        return maps.get(id);
    }

}
