package org.activityinfo.store.spi;

import com.google.common.base.Predicate;
import org.activityinfo.model.form.FormSyncSet;
import org.activityinfo.model.resource.ResourceId;

import java.util.List;

/**
 * FormStorage implementation whose records are versioned.
 *
 *
 */
public interface VersionedFormStorage extends FormStorage {

    /**
     * Retrieves a list of versions of this record.
     */
    List<RecordVersion> getVersions(ResourceId recordId);

    List<RecordVersion> getVersionsForParent(ResourceId parentRecordId);

    FormSyncSet getVersionRange(long localVersion, long toVersion, Predicate<ResourceId> visibilityPredicate);

}
