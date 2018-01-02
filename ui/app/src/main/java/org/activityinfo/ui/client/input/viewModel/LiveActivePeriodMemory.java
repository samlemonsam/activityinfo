package org.activityinfo.ui.client.input.viewModel;

import org.activityinfo.model.type.time.LocalDate;

public class LiveActivePeriodMemory implements ActivePeriodMemory {
    @Override
    public LocalDate getLastUsedDate() {
        return new LocalDate();
    }
}
