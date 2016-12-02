package org.activityinfo.core.shared.type.converter;

import org.activityinfo.model.type.FieldValue;

import javax.annotation.Nonnull;

/**
 * Converts raw imported values from the {@code ImportSource} to the
 * correct field value
 */
public interface FieldValueParser {

    /**
     * Converts the non-null {@code value} to the correct type
     */
    public FieldValue convert(@Nonnull String value);
}
