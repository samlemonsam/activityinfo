package org.activityinfo.ui.client.input.viewModel;

import org.activityinfo.model.type.time.LocalDate;

public class SimpleActivePeriodMemory implements ActivePeriodMemory {

    private LocalDate today = new LocalDate(2017, 10, 30);

    @Override
    public LocalDate getLastUsedDate() {
        return today;
    }
}
