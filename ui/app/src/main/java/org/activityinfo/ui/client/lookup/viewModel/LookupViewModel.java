package org.activityinfo.ui.client.lookup.viewModel;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.LookupKey;
import org.activityinfo.model.formTree.LookupKeySet;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.store.query.shared.FormSource;
import org.activityinfo.ui.client.input.viewModel.PermissionFilters;
import org.activityinfo.ui.client.lookup.model.LookupModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class LookupViewModel {

    private static final Logger LOGGER = Logger.getLogger(LookupViewModel.class.getName());

    private final ResourceId referencedFormId;
    private final LookupKeySet lookupKeySet;

    private final StatefulValue<LookupModel> model = new StatefulValue<>(new LookupModel());
    private final Map<LookupKey, Observable<Optional<String>>> selectedKeys;

    private final KeyMatrix keyMatrix;
    private final KeySelection keySelection;

    private final List<LookupKeyViewModel> levels = new ArrayList<>();

    private final Observable<Optional<RecordRef>> selectedRef;

    public LookupViewModel(FormSource formSource, FormTree formTree,
                           ReferenceType referenceType) {
        this(formSource, formTree, referenceType, Observable.just(Optional.absent()));
    }

    public LookupViewModel(FormSource formSource,
                           FormTree formTree,
                           ReferenceType referenceType,
                           Observable<Optional<ExprNode>> filter) {

        assert referenceType.getRange().size() == 1 : "Only single referenced form supported";

        this.referencedFormId = Iterables.getOnlyElement(referenceType.getRange());
        this.lookupKeySet = new LookupKeySet(formTree, referenceType);

        this.keyMatrix = new KeyMatrix(formSource, lookupKeySet, filter);

        /*
         * The labels that we display for each of the labels come EITHER from the
         * initial selection OR the explicit selection of the user.
         */
        Observable<RecordRef> initialSelection = model.transformIf(m -> m.getInitialSelection());

        Observable<Map<LookupKey, String>> initialSelectionLabels = keyMatrix.findKeyLabels(initialSelection);

        Observable<Map<LookupKey, String>> explicitSelection = model.transform(m -> m.getSelectedKeys());

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
        for (LookupKey key : lookupKeySet.getLookupKeys()) {
            selectedKeys.put(key, labels.transform(m -> Optional.fromNullable(m.get(key))));
        }

        this.keySelection = new KeySelection(keyMatrix, selectedKeys);

        /*
         * Now add a view model for each level
         */
        for (LookupKey lookupKey : lookupKeySet.getLookupKeys()) {
            levels.add(new LookupKeyViewModel(lookupKey,
                    keySelection.isEnabled(lookupKey),
                    keySelection.getSelectedKey(lookupKey),
                    keySelection.getChoices(lookupKey)));
        }

        /*
         * Find the selected ref
         */
        this.selectedRef = modelState.join(state -> {
            switch (state) {
                case INITIAL:
                    return model.transform(m -> m.getInitialSelection());
                default:
                    return keySelection.getSelectedRef();
            }
        });
    }

    public ResourceId getReferencedFormId() {
        return referencedFormId;
    }

    public LookupKey getLeafKey() {
        return lookupKeySet.getLeafKeys().get(0);
    }

    public List<LookupKeyViewModel> getLookupKeys() {
        return levels;
    }

    public void select(LookupKey lookupKey, String keyChoice) {

        Map<LookupKey, String> newSelectedKeys = new HashMap<>();
        newSelectedKeys.put(lookupKey, keyChoice);

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

    public Observable<Optional<RecordRef>> getSelectedRecord() {
        return selectedRef;
    }

    public void setInitialSelection(RecordRef recordRef) {
        model.updateIfNotEqual(new LookupModel(recordRef));
    }

    public void clearSelection() {
        model.updateIfNotEqual(new LookupModel());
    }
}
