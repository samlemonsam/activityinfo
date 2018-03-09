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
package org.activityinfo.geoadmin.merge2.view.mapping;

import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.geo.GeoArea;


public class GeoAreaFieldMapping implements FieldMapping {

    private FieldProfile sourceField;
    private FormField targetField;

    public GeoAreaFieldMapping(FieldProfile sourceField, FormField targetField) {
        this.sourceField = sourceField;
        this.targetField = targetField;
    }

    @Override
    public ResourceId getTargetFieldId() {
        return targetField.getId();
    }

    @Override
    public FieldValue mapFieldValue(int sourceIndex) {
        return new GeoArea(sourceField.getExtents(sourceIndex));
    }
}
