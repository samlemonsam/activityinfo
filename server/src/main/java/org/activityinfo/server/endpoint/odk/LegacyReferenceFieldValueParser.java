package org.activityinfo.server.endpoint.odk;

import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ReferenceValue;

import java.util.Iterator;
import java.util.Set;

class LegacyReferenceFieldValueParser implements FieldValueParser {
    final private char domain;

    LegacyReferenceFieldValueParser(Set<ResourceId> range) {
        char domain;
        Iterator<ResourceId> iterator = range.iterator();

        if (iterator.hasNext()) {
            domain = iterator.next().getDomain();
        } else {
            throw new IllegalArgumentException("A ReferenceType with an empty range cannot be used to parse instances");
        }

        for (ResourceId resourceId : range) {
            if (resourceId.getDomain() != domain) throw new IllegalArgumentException("A ReferenceType is inconsistent");
        }

        this.domain = Character.toLowerCase(domain);
    }

    @Override
    public FieldValue parse(String text) {
        if (text == null) throw new IllegalArgumentException("Malformed Element passed to OdkFieldValueParser.parse()");

        return new ReferenceValue(CuidAdapter.cuid(domain, Integer.parseInt(text)));
    }
}
