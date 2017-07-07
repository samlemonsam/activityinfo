package org.activityinfo.ui.client.store.offline;

import org.activityinfo.model.form.FormSyncSet;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.store.http.HttpStore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.activityinfo.observable.Observable.flatMap;


public class Snapshot {

    private List<FormMetadata> forms;
    private List<FormSyncSet> recordSets;


    public Snapshot(List<FormMetadata> forms, List<FormSyncSet> recordSets) {
        this.forms = forms;
        this.recordSets = recordSets;
    }

    public static Observable<Snapshot> compute(Observable<Set<ResourceId>> offlineForms, HttpStore httpStore) {

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
                for (FormClass form : tree.getFormClasses()) {
                    if(!isBuiltinForm(form.getId())) {
                        set.add(form.getId());
                    }
                }
            }
            return set;
        });

        // Now need fetch the latest version numbers of each of these forms
        Observable<List<FormMetadata>> metadata =  flatMap(completeSet, httpStore::getFormMetadata);

        // And finally fetch any difference between our current snapshot and the latest version of the new snapshot
        return metadata.join(forms -> {
            List<Observable<FormSyncSet>> recordSets = new ArrayList<>();
            for (FormMetadata form : forms) {
                recordSets.add(httpStore.getVersionRange(form.getId(), 0, form.getVersion()));
            }

            return Observable.flatten(recordSets).transform(x -> new Snapshot(forms, x));
        });
    }

    private static boolean isBuiltinForm(ResourceId formId) {
        if(formId.equals(GeoPointType.INSTANCE.getFormClass().getId())) {
            return true;
        }

        return false;
    }


    public List<FormMetadata> getForms() {
        return forms;
    }

    public List<FormSyncSet> getSyncSets() {
        return recordSets;
    }
}
