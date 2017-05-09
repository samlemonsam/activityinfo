package org.activityinfo.ui.client.store;

import org.activityinfo.api.client.FormRecordSet;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.store.http.FormMetadataRequest;
import org.activityinfo.ui.client.store.http.HttpBus;
import org.activityinfo.ui.client.store.http.VersionRangeRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;


public class Snapshot {

    private List<FormMetadata> forms;
    private List<FormRecordSet> recordSets;

    private Date snapshotTime;


    public Snapshot(List<FormMetadata> forms, List<FormRecordSet> recordSets) {
        this.forms = forms;
        this.recordSets = recordSets;
        this.snapshotTime = new Date();
    }

    public static Observable<Snapshot> compute(Observable<Set<ResourceId>> offlineForms, HttpBus httpBus) {

        Observable<List<FormMetadata>> metadata = offlineForms.join(forms -> {
            List<Observable<FormMetadata>> metadataList = new ArrayList<>();
            for (ResourceId formId : forms) {
                metadataList.add(httpBus.get(new FormMetadataRequest(formId)));
            }
            return Observable.flatten(metadataList);
        });

        return metadata.join(forms -> {
            List<Observable<FormRecordSet>> recordSets = new ArrayList<>();
            for (FormMetadata form : forms) {
                recordSets.add(httpBus.get(new VersionRangeRequest(form.getId(), 0, form.getVersion())));
            }

            return Observable.flatten(recordSets).transform(x -> new Snapshot(forms, x));
        });
    }

    public Date getSnapshotTime() {
        return snapshotTime;
    }

    public List<FormMetadata> getForms() {
        return forms;
    }

    public List<FormRecordSet> getRecordSets() {
        return recordSets;
    }
}
