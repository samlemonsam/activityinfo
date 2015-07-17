package org.activityinfo.server.endpoint.odk;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.image.ImageRowValue;
import org.activityinfo.model.type.image.ImageValue;

import static com.google.common.net.MediaType.ANY_IMAGE_TYPE;

class ImageFieldValueParser implements FieldValueParser {
    @Override
    public FieldValue parse(String text) {
        if (text == null) throw new IllegalArgumentException("Malformed Element passed to OdkFieldValueParser.parse()");

        return new ImageValue(new ImageRowValue(ANY_IMAGE_TYPE.toString(), text, ResourceId.generateId().asString()));
    }
}
