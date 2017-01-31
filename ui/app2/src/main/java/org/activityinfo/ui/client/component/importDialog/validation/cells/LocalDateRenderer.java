package org.activityinfo.ui.client.component.importDialog.validation.cells;

import com.google.common.base.Function;

import javax.annotation.Nullable;

/**
 * Renders a LocalDate to SafeHtml
 */
public class LocalDateRenderer implements Function<Object, String> {

    @Nullable
    @Override
    public String apply(@Nullable Object input) {
        return input == null ? null : input.toString();
    }
}
