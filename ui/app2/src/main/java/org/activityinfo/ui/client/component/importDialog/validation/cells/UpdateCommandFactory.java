package org.activityinfo.ui.client.component.importDialog.validation.cells;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import org.activityinfo.model.formTree.FieldPath;

public interface UpdateCommandFactory<T> {

    ScheduledCommand setColumnValue(FieldPath property, String value);

}
