package org.activityinfo.server.endpoint.odk;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.attachment.Attachment;
import org.activityinfo.model.type.attachment.AttachmentValue;

class AttachmentFieldValueParser implements FieldValueParser {
    @Override
    public FieldValue parse(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Malformed Element passed to OdkFieldValueParser.parse()");
        }

        return new AttachmentValue(new Attachment("image/*", text, ResourceId.generateId().asString()));
    }
}
