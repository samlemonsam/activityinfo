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
package org.activityinfo.ui.client;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.table.TablePlace;

public class AppPlaceHistoryMapper implements PlaceHistoryMapper {

    @Override
    public Place getPlace(String token) {
        String[] parts = token.split("/");
        if(parts[0].equals("table")) {
            return new TablePlace(ResourceId.valueOf(parts[1]));
        } else {
            return null;
        }
    }

    @Override
    public String getToken(Place place) {
        if(place instanceof TablePlace) {
            return "table/" + ((TablePlace) place).getFormId().asString();
        }
        return null;
    }
}
