package org.activityinfo.geoadmin.merge2;

import org.activityinfo.geoadmin.merge2.state.MergeModel2;
import org.activityinfo.geoadmin.merge2.state.ResourceStore;
import org.activityinfo.geoadmin.merge2.view.model.*;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;


public class MergeModelStore extends Observable<MergeModel2> {

    private ResourceStore resourceStore;
    private Map<>chn
    
    
    private MergeModel2 value;
    
    
    
    private Observable<FormTree> sourceTree;
    private Observable<FormProfile> sourceProfile;
    private Observable<FormTree> targetTree;
    private Observable<FormProfile> targetProfile;
    private Observable<FormMapping> fieldMapping;
    private Observable<AutoRowMatching> autoRowMatching;
    private final Observable<RowMatching> rowMatching;

    public MergeModelStore(final ResourceStore resourceStore, ResourceId source, ResourceId target) {
        this.resourceStore = resourceStore;
        this.value = new MergeModel2();
        this.value.setSourceFormId(source);
        this.value.setTargetFormId(target);
        
        this.sourceTree = resourceStore.getFormTree(source);
        this.targetTree = resourceStore.getFormTree(target);
        this.sourceProfile = FormProfile.profile(resourceStore, sourceTree);
        this.targetProfile = FormProfile.profile(resourceStore, targetTree);
        this.fieldMapping = FormMapping.compute(sourceProfile, targetProfile);
        this.autoRowMatching = fieldMapping.transform(new AutoMatcher());
        this.rowMatching = RowMatching.compute(autoRowMatching);
    }

    @Override
    public boolean isLoading() {
        return false;
    }

    @Override
    public MergeModel2 get() {
        return value;
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

    public Observable<RowMatching> getRowMatching() {
        return rowMatching;
    }
}
