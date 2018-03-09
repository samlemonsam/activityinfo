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

import com.extjs.gxt.ui.client.data.BaseModelData;
import org.activityinfo.legacy.shared.reports.model.layers.MapLayer;

public class LayerModel extends BaseModelData {
    private transient MapLayer mapLayer;

    public String getName() {
        return get("name");
    }

    public void setName(String name) {
        set("name", name);
    }

    public boolean isVisible() {
        return (Boolean) get("visible");
    }

    public void setVisible(boolean isVisible) {
        set("visible", isVisible);
    }

    public void setMapLayer(MapLayer mapLayer) {
        this.mapLayer = mapLayer;
    }

    public MapLayer getMapLayer() {
        return mapLayer;
    }

    public String getLayerType() {
        return get("type");
    }

    public void setLayerType(String type) {
        set("type", type);
    }
}