package org.activityinfo.ui.client.lookup.viewModel;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.Exprs;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.LookupKey;
import org.activityinfo.model.formTree.LookupKeySet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.store.query.shared.FormSource;
import org.activityinfo.ui.client.input.viewModel.ReferenceChoice;
import org.activityinfo.ui.client.lookup.model.LookupModel;

import java.util.*;

public class LookupViewModel {
    private final LookupKeySet lookupKeySet;
    private final List<LookupKeyViewModel> levels = new ArrayList<>();
    private final StatefulValue<LookupModel> model = new StatefulValue<>(new LookupModel());
    private final ResourceId referencedFormId;
    private FormSource formSource;
    private final Map<LookupKey, Observable<Optional<String>>> selectedKeys;

    public LookupViewModel(FormSource formSource, FormTree formTree, ReferenceType referenceType) {
        assert referenceType.getRange().size() == 1 : "Only single referenced form supported";

        this.formSource = formSource;
        this.lookupKeySet = new LookupKeySet(formTree, referenceType);
        this.referencedFormId = Iterables.getOnlyElement(referenceType.getRange());

        /*
         * The labels that we display for each of the labels come EITHER from the
         * initial selection OR the explicit selection of the user.
         */
        Observable<Map<LookupKey, String>> initialSelectionLabels =  model
            .transformIf(m -> m.getInitialSelection())
            .join(this::queryInitialSelectionLabels);

        Observable<Map<LookupKey, String>> explicitSelection = model
            .transform(m -> m.getSelectedKeys());

        Observable<LookupModel.State> modelState = model.transform(m -> m.getState()).cache();
        Observable<Map<LookupKey, String>> labels = modelState.join(state -> {
            switch (state) {
                case INITIAL:
                    return initialSelectionLabels;
                default:
                    return explicitSelection;
            }
        });

        /*
         * Go from an observable map to a map of observable values...
         */
        selectedKeys = new HashMap<>();
        for (LookupKey lookupKey : lookupKeySet.getLookupKeys()) {
            selectedKeys.put(lookupKey, labels.transform(m -> Optional.fromNullable(m.get(lookupKey))));
        }

        /*
         * Now add a view model for each level
         */
        for (LookupKey lookupKey : lookupKeySet.getLookupKeys()) {
            levels.add(new LookupKeyViewModel(formSource, lookupKey, selectedKeys));
        }
    }

    public ResourceId getReferencedFormId() {
        return referencedFormId;
    }

    public LookupKey getLeafKey() {
        return lookupKeySet.getLeafKeys().get(0);
    }

    /**
     * Given a reference to a selected record, query the corresponding lookup key labels
     */
    private Observable<Map<LookupKey, String>> queryInitialSelectionLabels(RecordRef ref) {
        assert lookupKeySet.getLeafKeys().size() == 1;

        LookupKey leafKey = lookupKeySet.getLeafKeys().get(0);
        assert leafKey.getFormId().equals(ref.getFormId());

        QueryModel model = new QueryModel(leafKey.getFormId());
        model.setFilter(Exprs.idEqualTo(ref.getRecordId()));

        Map<LookupKey, ExprNode> keyFormulas = leafKey.getKeys();
        for (ExprNode keyFormula : keyFormulas.values()) {
            model.selectExpr(keyFormula).as(keyFormula.asExpression());
        }

        return formSource.query(model).transform(columnSet -> {
            if(columnSet.getNumRows() == 1) {
                Map<LookupKey, String> labels = new HashMap<>();
                for (Map.Entry<LookupKey, ExprNode> entry : keyFormulas.entrySet()) {
                    LookupKey lookupKey = entry.getKey();
                    ColumnView columnView = columnSet.getColumnView(entry.getValue().asExpression());
                    String label = columnView.getString(0);
                    labels.put(lookupKey, label);
                }
                return labels;
            } else {
                return Collections.emptyMap();
            }
        });
    }

    public List<LookupKeyViewModel> getLookupKeys() {
        return levels;
    }

    public void select(LookupKey lookupKey, ReferenceChoice referenceChoice) {

        Map<LookupKey, String> newSelectedKeys = new HashMap<>();
        newSelectedKeys.put(lookupKey, referenceChoice.getLabel());

        LookupKey parentKey = lookupKey;
        while(!parentKey.isRoot()) {
            parentKey = parentKey.getParentLevel();
            String selectedKey = selectedKeys.get(parentKey).get().get();
            newSelectedKeys.put(parentKey, selectedKey);
        }

        model.updateIfNotEqual(new LookupModel(newSelectedKeys));
    }

    public void select(RecordRef ref) {
        model.updateIfNotEqual(new LookupModel(ref));
    }

    public Optional<RecordRef> getSelectedRecord() {
        return model.get().getInitialSelection();
    }

    public void setInitialSelection(RecordRef recordRef) {
        model.updateIfNotEqual(new LookupModel(recordRef));
    }

    public void clearSelection() {
        model.updateIfNotEqual(new LookupModel());
    }
}
