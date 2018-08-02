/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.ui.client.store.offline;

import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.form.FormSyncSet;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.store.http.HttpStore;

import java.util.*;

import static org.activityinfo.observable.Observable.flatMap;


/**
 * Data structure which contains the deltas to update the local snapshot to a new version set.
 *
 */
public class SnapshotDelta {

    private List<FormMetadata> forms;
    private List<FormSyncSet> recordSets;

    public SnapshotDelta(List<FormMetadata> forms, List<FormSyncSet> recordSets) {
        this.forms = forms;
        this.recordSets = recordSets;
    }

    public static Observable<Optional<SnapshotDelta>> compute(HttpStore httpStore, Observable<Set<ResourceId>> offlineForms, Observable<SnapshotStatus> currentStatus) {

        // We start with the "offlineForm" set which contains the set
        // of forms the user has explicitly asked to cache.

        // In order to find the related forms, we need the complete form trees of each of the
        // selected forms.
        Observable<List<FormTree>> formTrees = flatMap(offlineForms, httpStore::getFormTree);

        // Together, all the related forms constitute the set of forms we need for
        // a complete offline snapshot
        Observable<Set<ResourceId>> completeSet = formTrees.transform(trees -> {
            Set<ResourceId> set = new HashSet<>();
            for (FormTree tree : trees) {
                for (FormMetadata form : tree.getForms()) {
                    set.add(form.getId());
                }
            }
            return set;
        });

        // Now need fetch the latest version numbers of each of these forms
        Observable<List<FormMetadata>> metadata =  flatMap(completeSet, httpStore::getFormMetadata);

        // And finally fetch any difference between our current snapshot and the latest version of the new snapshot
        return Observable.join(currentStatus, metadata, (current, forms) -> {
            List<Observable<FormSyncSet>> recordSets = new ArrayList<>();

            for (FormMetadata form : forms) {
                long localVersion = current.getLocalVersion(form.getId());
                if(form.getVersion() > localVersion) {
                    recordSets.add(versionRange(httpStore, current, form));
                }
            }
            if(recordSets.isEmpty()) {
                return Observable.just(Optional.empty());
            } else {
                return Observable.flatten(recordSets).transform(x -> Optional.of(new SnapshotDelta(forms, x)));
            }
        });
    }

    private static Observable<FormSyncSet> versionRange(HttpStore httpStore, SnapshotStatus current, FormMetadata form) {
        long localVersion = current.getLocalVersion(form.getId());
        long targetVersion = form.getVersion();

        return fetchVersionRangeChunksRecursively(httpStore, form, localVersion, targetVersion, Optional.empty());
    }

    private static Observable<FormSyncSet> fetchVersionRangeChunksRecursively(
            HttpStore httpStore,
            FormMetadata form,
            long localVersion,
            long targetVersion,
            Optional<String> cursor) {

        Observable<FormSyncSet> chunk = httpStore.getVersionRange(form.getId(), localVersion, targetVersion, cursor);
        return chunk.join(c -> {
            if(c.isComplete()) {
                return Observable.just(c);
            } else {
                Observable<FormSyncSet> nextChunk = fetchVersionRangeChunksRecursively(httpStore, form, localVersion, targetVersion, Optional.of(c.getCursor()));
                return nextChunk.transform(n -> FormSyncSet.foldLeft(c, n));
            }
        });
    }

    public List<FormMetadata> getForms() {
        return forms;
    }

    public List<FormSyncSet> getSyncSets() {
        return recordSets;
    }
}
