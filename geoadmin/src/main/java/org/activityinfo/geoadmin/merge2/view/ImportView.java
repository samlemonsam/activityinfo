package org.activityinfo.geoadmin.merge2.view;

import com.google.common.base.Function;
import org.activityinfo.geoadmin.merge2.model.ImportModel;
import org.activityinfo.geoadmin.merge2.view.mapping.FieldMapping;
import org.activityinfo.geoadmin.merge2.view.mapping.FormMapping;
import org.activityinfo.geoadmin.merge2.view.match.*;
import org.activityinfo.geoadmin.merge2.view.profile.FormProfile;
import org.activityinfo.geoadmin.merge2.view.swing.SwingSchedulers;
import org.activityinfo.geoadmin.model.TransactionBuilder;
import org.activityinfo.geoadmin.model.UpdateBuilder;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.legacy.KeyGenerator;
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
    private final Observable<InstanceMatchGraph> matchGraph;
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
        mapping = FormMapping.computeFromMatching(store, keyFields, model.getReferenceMatches());

        matchGraph = InstanceMatchGraph.build(scheduler, keyFields);
        matchTable = new MatchTable(model, matchGraph);
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


    /**
     * Based on the users explict choices and the automatic matching / mapping, 
     * build a transaction to effect the import.
     */
    public TransactionBuilder buildTransaction() {
        TransactionBuilder tx = new TransactionBuilder();

        ResourceId targetClassId = model.getTargetFormId().get();
        
        KeyGenerator generator = new KeyGenerator();

        MatchTable matchTable = getMatchTable();
        int numRows = matchTable.getRowCount();
        for (int i = 0; i < numRows; i++) {
            MatchRow matchRow = matchTable.get(i);
            if (!matchRow.isMatched(MatchSide.SOURCE)) {
                // no corresponding row in the source:
                // delete unmatched target
                tx.delete(matchRow.getTargetId().get());

            } else {

                UpdateBuilder update;
                if (matchRow.isMatched(MatchSide.TARGET)) {
                    // update target with properties from the source
                    update = tx.update(matchRow.getTargetId().get());
                } else {
                    // create a new instance with properties from the source
                    update = tx.create(targetClassId, CuidAdapter.entity(generator.generateInt()));
                }
                
                // apply properties from field mapping
                for (FieldMapping fieldMapping : mapping.get().getFieldMappings()) {
                    update.setProperty(
                            fieldMapping.getTargetFieldId(), 
                            fieldMapping.mapFieldValue(matchRow.getSourceRow()));
                }
                
            }
        }
        return tx;
    }
}


