package org.activityinfo.ui.client.input.viewModel;

import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.model.type.time.MonthValue;

public class SimpleActivePeriodMemory implements ActivePeriodMemory {

    private LocalDate today = new LocalDate(2017, 10, 30);

    @Override
    public FieldValue getActivePeriod(SubFormKind subFormKind) {
        switch (subFormKind) {
            case MONTHLY:
                return new MonthValue(today.getYear(), today.getMonthOfYear());

            default:
                throw new UnsupportedOperationException("TODO: " + subFormKind);
        }
    }
}
