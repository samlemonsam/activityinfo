package org.activityinfo.ui.client.component.importDialog.model.type.converter;

import org.activityinfo.io.match.date.LatinDateParser;
import org.activityinfo.model.type.time.LocalDate;

import javax.annotation.Nonnull;

/**
 * Parses strings to local dates
 */
public class LocalDateParser implements FieldValueParser {

    private LatinDateParser dateParser = new LatinDateParser();

    @Nonnull
    @Override
    public LocalDate convert(@Nonnull String string) {
        return dateParser.parse(string);
    }
}
