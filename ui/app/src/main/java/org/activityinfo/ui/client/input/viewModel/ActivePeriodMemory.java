package org.activityinfo.ui.client.input.viewModel;

import org.activityinfo.model.type.time.LocalDate;

/**
 * Remembers the period of time the user was working with
 */
public interface ActivePeriodMemory {

    LocalDate getLastUsedDate();
}
