package org.activityinfo.server.endpoint.odk;

import com.google.api.client.util.Lists;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumValue;

import java.util.List;

import static org.activityinfo.model.legacy.CuidAdapter.attributeField;

public class LegacyEnumFieldValueParser implements FieldValueParser {
    @Override
    public FieldValue parse(String text) {
        if (text == null) throw new IllegalArgumentException("Malformed Element passed to OdkFieldValueParser.parse()");

        String selected[] = text.split(" ");
        List<ResourceId> resourceIds = Lists.newArrayListWithCapacity(selected.length);

        for (String item : selected) {
            if (item.length() < 1) continue;
            resourceIds.add(attributeField(Integer.parseInt(item)));
        }

        return new EnumValue(resourceIds);
    }
}
