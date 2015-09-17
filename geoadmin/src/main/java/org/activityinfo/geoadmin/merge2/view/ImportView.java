package org.activityinfo.geoadmin.merge2.view;

import com.google.common.base.Function;
import org.activityinfo.geoadmin.merge2.model.ImportModel;
import org.activityinfo.geoadmin.merge2.view.mapping.FormMapping;
import org.activityinfo.geoadmin.merge2.view.match.*;
import org.activityinfo.geoadmin.merge2.view.profile.FormProfile;
import org.activityinfo.geoadmin.merge2.view.swing.SwingSchedulers;
import org.activityinfo.geoadmin.model.TransactionBuilder;
import org.activityinfo.geoadmin.model.UpdateBuilder;
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
    private final Observable<MatchGraph> matchGraph;
    private final Observable<KeyFieldPairSet> keyFields;
    private final MatchTable matchTable;
    private final Observable<FormMapping> mapping;


    public ImportView(ResourceStore store, ImportModel model) {
        this.store = store;
        this.model = model;
        
        scheduler = SwingSchedulers.fromExecutor(Executors.newCachedThreadPool());
        
        sourceProfile = profile(model.getSourceFormId());
        targetProfile = profile(model.getTargetFormId());
        keyFields = KeyFieldPairSet.compute(sourceProfile, targetProfile);
        matchGraph = MatchGraph.build(scheduler, keyFields);
        matchTable = new MatchTable(model, matchGraph);
        mapping = FormMapping.computeFromMatching(store, keyFields);
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

    public Observable<FormMapping> getMapping() {
        return mapping;
    }

    public ImportModel getModel() {
        return model;
    }
    
    
    public void buildTransaction() {
        TransactionBuilder tx = new TransactionBuilder();

        ResourceId targetClassId = model.getTargetFormId().get();

        MatchTable matchTable = getMatchTable();
        int numRows = matchTable.getRowCount();
        for (int i = 0; i < numRows; i++) {
            MatchRow matchRow = matchTable.get(i);
            if (matchRow.isMatched()) {
                // update target with properties from the source
                TransactionBuilder update = tx.update(matchRow.getTargetId().get());
                
            } else if(matchRow.isMatched(MatchSide.SOURCE)) {
                // create new source row
                UpdateBuilder update = tx.create(targetClassId, ResourceId.generateId());
            
            } else {
                // delete unmatched target
                tx.delete(matchRow.getTargetId().get());
            }
        }
    }
}


