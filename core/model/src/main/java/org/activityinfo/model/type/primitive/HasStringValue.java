package org.activityinfo.model.type.primitive;

import org.activityinfo.model.type.FieldValue;

/**
 * Marker interface for {@code FieldValue}s which can be represented, without loss of information,
 * as a {@code String} value. To qualify, this FieldValue's String value should not depend on locale or other
 * environmental factors; such types require converters or formatters.
 */
public interface HasStringValue extends FieldValue {

    String asString();

}
