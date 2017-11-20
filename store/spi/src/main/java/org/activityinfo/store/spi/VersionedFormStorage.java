package org.activityinfo.store.spi;

import com.google.common.base.Predicate;
import org.activityinfo.model.form.FormSyncSet;
import org.activityinfo.model.resource.ResourceId;

/**
 * FormStorage implementation whose records are versioned.
 */
public interface VersionedFormStorage extends FormStorage {

    FormSyncSet getVersionRange(long localVersion, long toVersion, Predicate<ResourceId> visibilityPredicate);

}
