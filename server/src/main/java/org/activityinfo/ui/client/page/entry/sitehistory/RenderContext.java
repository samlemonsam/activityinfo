package org.activityinfo.ui.client.page.entry.sitehistory;

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

import org.activityinfo.legacy.shared.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class RenderContext {
    private SchemaDTO schema;
    private Map<Integer, LocationDTO> locations;
    private SiteDTO site;
    private SiteHistoryDTO history;
    private Map<String, Object> state;
    private ActivityFormDTO form;

    RenderContext(ActivityFormDTO form, List<LocationDTO> locations, SiteDTO site, Map<String, Object> baselineState) {
        this.form = form;
        this.locations = new HashMap<Integer, LocationDTO>();
        for (LocationDTO dto : locations) {
            this.locations.put(dto.getId(), dto);
        }
        this.site = site;
        this.state = baselineState;
    }

    SchemaDTO getSchema() {
        return schema;
    }

    LocationDTO getLocation(Integer locationId) {
        return locations.get(locationId);
    }

    SiteDTO getSite() {
        return site;
    }

    SiteHistoryDTO getHistory() {
        return history;
    }

    void setHistory(SiteHistoryDTO history) {
        this.history = history;
    }

    Map<String, Object> getState() {
        return state;
    }

    public ActivityFormDTO getForm() {
        return form;
    }
}