package org.activityinfo.legacy.shared.adapter.projection;
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

import org.activityinfo.core.shared.Projection;
import org.activityinfo.legacy.shared.model.LocationDTO;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.type.geo.GeoPoint;
import org.activityinfo.model.type.primitive.TextValue;

/**
 * @author yuriyz on 4/15/14.
 */
class LocationFieldProjectionUpdater implements ProjectionUpdater<LocationDTO> {

    private FieldPath path;
    private int fieldIndex;

    LocationFieldProjectionUpdater(FieldPath path, int fieldIndex) {
        this.path = path;
        this.fieldIndex = fieldIndex;
    }

    @Override
    public void update(Projection projection, LocationDTO locationDTO) {
        switch (fieldIndex) {
            case CuidAdapter.NAME_FIELD:
                projection.setValue(path, TextValue.valueOf(locationDTO.getName()));
                break;
            case CuidAdapter.AXE_FIELD:
                projection.setValue(path, TextValue.valueOf(locationDTO.getAxe()));
                break;
            case CuidAdapter.GEOMETRY_FIELD:
                if (locationDTO.hasCoordinates()) {
                    projection.setValue(path, new GeoPoint(locationDTO.getLatitude(), locationDTO.getLongitude()));
                }
                break;
        }
    }
}
