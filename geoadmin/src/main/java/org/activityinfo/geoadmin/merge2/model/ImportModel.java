package org.activityinfo.geoadmin.merge2.model;

import org.activityinfo.store.ResourceStore;
import org.activityinfo.geoadmin.merge2.view.model.*;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;


public class ImportModel {

    private ResourceStore resourceStore;
    private StatefulValue<ResourceId> sourceFormId = new StatefulValue<>();
    private StatefulValue<ResourceId> targetFormId = new StatefulValue<>();
    private Observable<FormTree> sourceTree;
    private Observable<FormProfile> sourceProfile;
    private Observable<FormTree> targetTree;
    private Observable<FormProfile> targetProfile;
    private Observable<FormMapping> fieldMapping;
    private Observable<AutoRowMatching> autoRowMatching;
    private final Observable<MatchTable> rowMatching;

    public ImportModel(final ResourceStore resourceStore, ResourceId source, ResourceId target) {
        this.resourceStore = resourceStore;
        this.sourceFormId.updateValue(source);
        this.targetFormId.updateValue(target);
        
        this.sourceTree = resourceStore.getFormTree(source);
        this.targetTree = resourceStore.getFormTree(target);
        this.sourceProfile = FormProfile.profile(resourceStore, sourceTree);
        this.targetProfile = FormProfile.profile(resourceStore, targetTree);
        this.fieldMapping = FormMapping.compute(sourceProfile, targetProfile);
        this.autoRowMatching = fieldMapping.transform(new AutoMatcher());
        this.rowMatching = MatchTable.compute(autoRowMatching);
    }

    /**
     * @return the {@code ResourceId} of the form into which we are importing new data
     */
    public Observable<ResourceId> getTargetFormId() {
        return targetFormId;
    }

    public Observable<FormTree> getSourceTree() {
        return sourceTree;
    }

    public Observable<FormProfile> getSourceProfile() {
        return sourceProfile;
    }

    public Observable<FormTree> getTargetTree() {
        return targetTree;
    }

    public Observable<FormProfile> getTargetProfile() {
        return targetProfile;
    }

    public Observable<FormMapping> getFieldMapping() {
        return fieldMapping;
    }

    public Observable<MatchTable> getRowMatching() {
        return rowMatching;
    }
}
