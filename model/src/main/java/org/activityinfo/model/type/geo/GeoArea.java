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
package org.activityinfo.model.type.geo;

import org.activityinfo.json.JsonValue;
import org.activityinfo.model.type.FieldTypeClass;

import static org.activityinfo.json.Json.createObject;

/* A Field Value describing a geographic area on the Earth's surface
* in the WGS84 geographic reference system.
*/
public class GeoArea implements GeoFieldValue {

    private Extents envelope;
    private String blobId;

    public GeoArea(Extents envelope, String blobId) {
        this.envelope = envelope;
        this.blobId = blobId;
    }

    public GeoArea(Extents envelope) {
        this.envelope = envelope;
    }


    public Extents getEnvelope() {
        return envelope;
    }

    public String getBlobId() {
        return blobId;
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return GeoAreaType.TYPE_CLASS;
    }


    @Override
    public JsonValue toJson() {
        JsonValue object = createObject();
        object.put("blobId", blobId);
        object.put("bbox", envelope.toJsonElement());
        return object;
    }

}
