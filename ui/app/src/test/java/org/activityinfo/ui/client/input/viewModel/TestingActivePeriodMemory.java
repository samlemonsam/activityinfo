package org.activityinfo.ui.client.input.viewModel;

import org.activityinfo.model.type.time.LocalDate;

/**
 * A "memory" for the most recently used period that always returns the same date to keep
 * unit tests consistent.
 */
public class TestingActivePeriodMemory implements ActivePeriodMemory {

    private LocalDate today = new LocalDate(2017, 10, 30);

    @Override
    public LocalDate getLastUsedDate() {
        return today;
    }
}
