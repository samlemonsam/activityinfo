package org.activityinfo.store.spi;

import org.activityinfo.model.form.FormRecord;

import java.util.List;

/**
 * FormStorage implementation whose records are versioned.
 */
public interface VersionedFormStorage extends FormStorage {

    List<FormRecord> getVersionRange(long localVersion, long toVersion);

}
