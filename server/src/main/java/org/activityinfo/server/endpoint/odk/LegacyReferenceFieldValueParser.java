package org.activityinfo.server.endpoint.odk;

import com.google.common.collect.Iterables;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;

import java.util.Collection;

class LegacyReferenceFieldValueParser implements FieldValueParser {
    final private char domain;
    private final ResourceId formId;

    LegacyReferenceFieldValueParser(Collection<ResourceId> range) {
        formId = Iterables.getOnlyElement(range);
        domain = Character.toLowerCase(formId.getDomain());
    }

    @Override
    public FieldValue parse(String text) {
        if (text == null) throw new IllegalArgumentException("Malformed Element passed to OdkFieldValueParser.parse()");

        return new ReferenceValue(new RecordRef(formId, CuidAdapter.cuid(domain, Integer.parseInt(text))));
    }
}
