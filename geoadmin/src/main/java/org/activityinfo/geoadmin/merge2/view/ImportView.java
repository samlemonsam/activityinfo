package org.activityinfo.geoadmin.merge2.view;

import com.google.common.base.Function;
import org.activityinfo.geoadmin.merge2.model.ImportModel;
import org.activityinfo.geoadmin.merge2.view.match.MatchTable;
import org.activityinfo.geoadmin.merge2.view.profile.FormProfile;
import org.activityinfo.geoadmin.merge2.view.swing.SwingSchedulers;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Scheduler;
import org.activityinfo.store.ResourceStore;

import javax.annotation.Nullable;
import java.util.concurrent.Executors;


public class ImportView {

    private final ImportModel model;
    private final ResourceStore store;
    private final Scheduler scheduler;
    
    private final Observable<FormProfile> sourceProfile;
    private final Observable<FormProfile> targetProfile;
    private final MatchTable matchTable;


    public ImportView(ResourceStore store, ImportModel model) {
        this.store = store;
        this.model = model;
        
        scheduler = SwingSchedulers.fromExecutor(Executors.newCachedThreadPool());
        
        sourceProfile = profile(model.getSourceFormId());
        targetProfile = profile(model.getTargetFormId());
        matchTable = new MatchTable(model, scheduler, sourceProfile, targetProfile);
    }

    private Observable<FormProfile> profile(Observable<ResourceId> formId) {
        Observable<FormTree> formTree = formId.join(new Function<ResourceId, Observable<FormTree>>() {
            @Nullable
            @Override
            public Observable<FormTree> apply(ResourceId input) {
                return store.getFormTree(input);
            }
        });
    
        return FormProfile.profile(store, formTree);
    }

    public Observable<FormProfile> getSourceProfile() {
        return sourceProfile;
    }

    public Observable<FormProfile> getTargetProfile() {
        return targetProfile;
    }

    public MatchTable getMatchTable() {
        return matchTable;
    }

    public ImportModel getModel() {
        return model;
    }
}


