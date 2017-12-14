package org.activityinfo.store.spi;

import org.activityinfo.model.form.RecordHistory;
import org.activityinfo.model.form.RecordHistoryEntry;
import org.activityinfo.model.type.RecordRef;

import java.sql.SQLException;
import java.util.List;

public interface RecordHistoryProvider {
    RecordHistory build(RecordRef recordRef) throws SQLException;
}
