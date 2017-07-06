package org.activityinfo.store.spi;

import org.activityinfo.model.form.FormSyncSet;

/**
 * FormStorage implementation whose records are versioned.
 */
public interface VersionedFormStorage extends FormStorage {

    FormSyncSet getVersionRange(long localVersion, long toVersion);

}
