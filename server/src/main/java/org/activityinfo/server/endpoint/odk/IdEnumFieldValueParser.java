package org.activityinfo.server.endpoint.odk;

import com.google.api.client.util.Lists;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ReferenceValue;

import java.util.List;

class IdEnumFieldValueParser implements FieldValueParser {
    @Override
    public FieldValue parse(String text) {
        if (text == null) throw new IllegalArgumentException("Malformed Element passed to OdkFieldValueParser.parse()");

        String selected[] = text.split(" ");
        List<ResourceId> resourceIds = Lists.newArrayListWithCapacity(selected.length);

        for (String item : selected) {
            if (item.length() < 1) continue;
            resourceIds.add(ResourceId.valueOf(item));
        }

        return new ReferenceValue(resourceIds); // ReferenceValue not EnumFieldValue, since that way it'll at least work
    }
}
