package org.activityinfo.store.spi;

import org.activityinfo.model.form.FormHistoryEntry;
import org.activityinfo.model.type.RecordRef;

import java.sql.SQLException;
import java.util.List;

public interface RecordHistoryProvider {
    List<FormHistoryEntry> build(RecordRef recordRef) throws SQLException;
}
