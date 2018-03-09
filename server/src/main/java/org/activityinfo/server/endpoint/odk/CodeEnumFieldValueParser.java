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
package org.activityinfo.server.endpoint.odk;

import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;

import java.util.List;
import java.util.Map;

class CodeEnumFieldValueParser implements FieldValueParser {
    final private Map<String, ResourceId> values;

    CodeEnumFieldValueParser(EnumType enumType) {
        values = Maps.newHashMapWithExpectedSize(enumType.getValues().size());

        for (EnumItem value : enumType.getValues()) {
            values.put(value.getCode(), value.getId());
        }
    }

    @Override
    public FieldValue parse(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Malformed Element passed to OdkFieldValueParser.parse()");
        }

        String selected[] = text.split(" ");
        List<ResourceId> resourceIds = Lists.newArrayListWithCapacity(selected.length);

        for (String item : selected) {
            ResourceId resourceId = values.get(item);
            if (resourceId != null) resourceIds.add(resourceId);
        }

        return new EnumValue(resourceIds);
    }
}
