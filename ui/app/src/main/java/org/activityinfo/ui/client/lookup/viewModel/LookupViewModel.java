package org.activityinfo.ui.client.lookup.viewModel;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.LookupKey;
import org.activityinfo.model.formTree.LookupKeySet;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.store.query.shared.FormSource;
import org.activityinfo.ui.client.lookup.model.LookupModel;

import java.util.*;
import java.util.logging.Logger;

public class LookupViewModel {

    private static final Logger LOGGER = Logger.getLogger(LookupViewModel.class.getName());

    private final LookupKeySet lookupKeySet;

    private final StatefulValue<LookupModel> model = new StatefulValue<>(new LookupModel());
    private final Map<LookupKey, Observable<Optional<String>>> selectedKeys;

    /**
     * Map from each referenced form to it's key matrix.
     */
    private final KeyMatrixSet keyMatrixSet;

    private final KeySelectionSet keySelection;

    private final List<LookupKeyViewModel> levels = new ArrayList<>();

    private final Observable<Set<RecordRef>> selectedRef;

    public LookupViewModel(FormSource formSource, FormTree formTree,
                           FormField referenceField) {
        this(formSource, formTree, referenceField, Observable.just(Optional.absent()));
    }

    public LookupViewModel(FormSource formSource,
                           FormTree formTree,
                           FormField referenceField,
                           Observable<Optional<ExprNode>> filter) {

        this.lookupKeySet = new LookupKeySet(formTree, referenceField);

        this.keyMatrixSet = new KeyMatrixSet(formSource, (ReferenceType) referenceField.getType(), lookupKeySet, filter);

        /*
         * The labels that we display for each of the labels come EITHER from the
         * initial selection OR the explicit selection of the user.
         */
        Observable<Set<RecordRef>> initialSelection = model.transform(m -> m.getInitialSelection());

        Observable<Map<LookupKey, String>> initialSelectionLabels = keyMatrixSet.findKeyLabels(initialSelection);

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

        this.keySelection = new KeySelectionSet(keyMatrixSet, selectedKeys);

        /*
         * Now add a view model for each level
         */
        for (LookupKey lookupKey : lookupKeySet.getLookupKeys())
            levels.add(new LookupKeyViewModel(lookupKey,
                    keySelection.isEnabled(lookupKey),
                    keySelection.getSelectedKey(lookupKey),
                    keySelection.getChoices(lookupKey)));

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

    public List<LookupKeyViewModel> getLookupKeys() {
        return levels;
    }

    public void select(LookupKey lookupKey, String keyChoice) {

        Map<LookupKey, String> newSelectedKeys = new HashMap<>();
        for (Map.Entry<LookupKey, Observable<Optional<String>>> entry : selectedKeys.entrySet()) {
            Optional<String> selectedKey = entry.getValue().get();
            if(selectedKey.isPresent()) {
                newSelectedKeys.put(entry.getKey(), selectedKey.get());
            }
        }
        newSelectedKeys.put(lookupKey, keyChoice);

        // Clear the child choices
        clearChildChoices(lookupKey, newSelectedKeys);

        model.updateIfNotEqual(new LookupModel(newSelectedKeys));
    }

    private void clearChildChoices(LookupKey lookupKey, Map<LookupKey, String> newSelectedKeys) {
        for (LookupKey childKey : lookupKey.getChildLevels()) {
            if(newSelectedKeys.remove(childKey) != null) {
                clearChildChoices(childKey, newSelectedKeys);
            }
        }
    }

    public void select(RecordRef ref) {
        model.updateIfNotEqual(new LookupModel(ref));
    }

    public Observable<Set<RecordRef>> getSelectedRecords() {
        return selectedRef;
    }

    public Observable<Optional<RecordRef>> getSelectedRecord() {
        return selectedRef.transform(set -> {
            if(set.isEmpty()) {
                return Optional.absent();
            } else {
                return Optional.of(Iterables.getOnlyElement(set));
            }
        });
    }

    public void setInitialSelection(Set<RecordRef> recordRef) {
        model.updateIfNotEqual(new LookupModel(recordRef));
    }

    public void clearSelection() {
        model.updateIfNotEqual(new LookupModel());
    }
}
