package org.activityinfo.server.endpoint.odk;

import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;

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
        if (text == null) throw new IllegalArgumentException("Malformed Element passed to OdkFieldValueParser.parse()");

        String selected[] = text.split(" ");
        List<ResourceId> resourceIds = Lists.newArrayListWithCapacity(selected.length);

        for (String item : selected) {
            ResourceId resourceId = values.get(item);
            if (resourceId != null) resourceIds.add(resourceId);
        }

        return new ReferenceValue(resourceIds); // ReferenceValue not EnumFieldValue, since that way it'll at least work
    }
}
