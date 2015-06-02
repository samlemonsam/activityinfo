package org.activityinfo.geoadmin.merge2.model;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.ObservableSet;
import org.activityinfo.observable.StatefulSet;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.store.ResourceStore;


public class ImportModel {

    private ResourceStore resourceStore;
    private StatefulValue<ResourceId> sourceFormId = new StatefulValue<>();
    private StatefulValue<ResourceId> targetFormId = new StatefulValue<>();
    private InstanceMatchSet instanceMatchSet = new InstanceMatchSet();
    private ObservableSet<ReferenceMatch> referenceMatches = new StatefulSet<>();


    public ImportModel(final ResourceStore resourceStore, ResourceId source, ResourceId target) {
        this.resourceStore = resourceStore;
        this.sourceFormId.updateValue(source);
        this.targetFormId.updateValue(target);
    }

    public InstanceMatchSet getInstanceMatchSet() {
        return instanceMatchSet;
    }

    public StatefulValue<ResourceId> getSourceFormId() {
        return sourceFormId;
    }
    

    /**
     * @return the {@code ResourceId} of the form into which we are importing new data
     */
    public Observable<ResourceId> getTargetFormId() {
        return targetFormId;
    }

    public ObservableSet<ReferenceMatch> getReferenceMatches() {
        return referenceMatches;
    }
}
