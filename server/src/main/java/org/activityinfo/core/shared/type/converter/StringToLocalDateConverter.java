package org.activityinfo.core.shared.type.converter;

import com.bedatadriven.rebar.time.calendar.LocalDate;
import org.activityinfo.io.match.date.LatinDateParser;

import javax.annotation.Nonnull;

/**
 * Parses strings to local dates
 */
public class StringToLocalDateConverter implements StringConverter<LocalDate> {

    private LatinDateParser dateParser = new LatinDateParser();

    @Nonnull
    @Override
    public LocalDate convert(@Nonnull String string) {
        org.activityinfo.model.type.time.LocalDate localDate = dateParser.parse(string);
        return new LocalDate(localDate.getYear(), localDate.getMonthOfYear(), localDate.getDayOfMonth());
    }
        
}
