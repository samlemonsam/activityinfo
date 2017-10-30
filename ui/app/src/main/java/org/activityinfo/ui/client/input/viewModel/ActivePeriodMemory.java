package org.activityinfo.ui.client.input.viewModel;

import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.type.FieldValue;

public interface ActivePeriodMemory {

    FieldValue getActivePeriod(SubFormKind subFormKind);
}
