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

import com.google.common.base.Optional;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.analysis.AnalysisPlace;
import org.activityinfo.ui.client.catalog.CatalogPlace;
import org.activityinfo.ui.client.input.RecordPlace;
import org.activityinfo.ui.client.table.TablePlace;

public class AppPlaceHistoryMapper implements PlaceHistoryMapper {

    @Override
    public Place getPlace(String token) {
        String[] parts = token.split("/");
        if(parts[0].equals("table")) {
            return new TablePlace(ResourceId.valueOf(parts[1]));
        } else if(parts[0].equals("analysis")) {
            if(parts.length > 1) {
                return new AnalysisPlace(parts[1]);
            } else {
                return new AnalysisPlace(ResourceId.generateCuid());
            }

        } else if(parts[0].equals("record")) {
            ResourceId formId = ResourceId.valueOf(parts[1]);
            ResourceId recordId = ResourceId.valueOf(parts[2]);
            return new RecordPlace(formId, recordId);

        } else if(parts[0].equals("catalog")) {
            Optional<String> parentId = Optional.absent();
            if(parts.length > 1) {
                parentId = Optional.of(parts[1]);
            }
            return new CatalogPlace(parentId);

        } else {
            return null;
        }
    }

    @Override
    public String getToken(Place place) {
        if(place instanceof TablePlace) {
            return "table/" + ((TablePlace) place).getFormId().asString();
        } else if(place instanceof AnalysisPlace) {
            return "analysis/" + ((AnalysisPlace) place).getId();
        } else if(place instanceof RecordPlace) {
            return "record/" + ((RecordPlace) place).getFormId() + "/" + ((RecordPlace) place).getRecordId();
        } else if(place instanceof CatalogPlace) {
            Optional<String> parentId = ((CatalogPlace) place).getParentId();
            if(parentId.isPresent()) {
                return "catalog/" + parentId.get();
            } else {
                return "catalog";
            }
        }
        return null;
    }
}
