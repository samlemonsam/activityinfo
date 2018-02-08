package org.activityinfo.ui.client.lookup.viewModel;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.activityinfo.model.formTree.LookupKey;
import org.activityinfo.model.formTree.LookupKeySet;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;

import java.util.*;

/**
 * Unions a set of {@link KeyMatrixSet}
 */
public class KeySelectionSet {

    private final LookupKeySet lookupKeySet;
    private final List<KeySelection> selections;
    private final Map<LookupKey, Observable<Optional<String>>> selectedKeys;
    private final Observable<Set<RecordRef>> selectedRefs;

    public KeySelectionSet(KeyMatrixSet keyMatrixSet, Map<LookupKey, Observable<Optional<String>>> selectedKeys) {
        lookupKeySet = keyMatrixSet.getLookupKeySet();
        selections = new ArrayList<>();
        for (KeyMatrix matrix : keyMatrixSet.getMatrices()) {
            selections.add(new KeySelection(matrix, selectedKeys));
        }
        this.selectedKeys = selectedKeys;
        this.selectedRefs = Observable.flatJoin(selections, s -> s.getSelectedRef()).transform(this::normalize);
    }


    /**
     * Returns the value of the key currently selected by the user, if any.
     */
    public Observable<Optional<String>> getSelectedKey(LookupKey key) {
        return selectedKeys.get(key);
    }

    public Observable<Boolean> isEnabled(LookupKey key) {
        if(key.isRoot()) {
            return Observable.just(true);
        } else {
            return getSelectedKey(key.getParentLevel()).transform(pk -> pk.isPresent());
        }
    }

    public Observable<List<String>> getChoices(LookupKey lookupKey) {
        List<Observable<List<String>>> choiceSets = new ArrayList<>();
        for (KeySelection keySelection : selections) {
            if(keySelection.containsLookupKey(lookupKey)) {
                choiceSets.add(keySelection.getChoices(lookupKey));
            }
        }

        assert choiceSets.size() > 0;

        return Observable.flatten(choiceSets).transform(lists -> {
            if(lists.size() == 1) {
                return lists.get(0);
            } else {
                return distinct(lists);
            }
        });
    }

    /**
     * Takes a list of sorted, distinct lists and returns a sorted list containing
     * all of the items that are in at least one list.
     *
     * We _could_ take advantage of the fact that each of the input lists is already sorted
     * and distinct
     * <pre>
     *
     *  lists[0]    lists[1]     OUTPUT
     *  --------    --------     ------
     *  A           A            A
     *  B           C            B
     *  D           D            C
     *  F           E            D
     *              F            E
     *                           F
     * </pre>
     *
     */
    private List<String> distinct(List<List<String>> lists) {

        // But this solutions is the easiest to code...

        Set<String> set = new HashSet<>();
        for (List<String> list : lists) {
            set.addAll(list);
        }

        List<String> output = Lists.newArrayList(set);
        Collections.sort(output);

        return output;
    }

    /**
     * When we have overlapping hierarchies, we don't include all complete selections as
     * the value for the field.
     *
     * For example, given a reference field that includes [PROVINCE, TERRITORY] in the range, we will
     * have two {@link KeySelection} for each of the forms in this range. Their key trees look like this:
     *
     * <pre>
     *
     *     PROVINCE               TERRITORY
     *
     *     k0: Province.Name      k0: Province.Name
     *                                 ^
     *                                 |
     *                            k1: Territory.Name
     *
     * </pre>
     *
     * Once the user has selected both the Province.Name (k0) and the Territory.Name (k1), then we have a valid
     * selection for both forms.
     *
     * However, in this case, we don't want to store _both_ the territory _and_ the province as this not really
     * the user's intent. We exclude the province selection because it is implied by the territory selection.
     *
     *
     */
    private Set<RecordRef> normalize(List<Optional<RecordRef>> refs) {

        // Build a map from referenced form id -> selection
        Map<ResourceId, RecordRef> selectionMap = new HashMap<>();
        for (int i = 0; i < selections.size(); i++) {
            if(refs.get(i).isPresent()) {
                selectionMap.put(selections.get(i).getFormId(), refs.get(i).get());
            }
        }
        List<ResourceId> selectedFormIds = Lists.newArrayList(selectionMap.keySet());

        // Now remove redundant parents
        for (ResourceId formId : selectedFormIds) {
            for (ResourceId ancestorFormId : lookupKeySet.getAncestorForms(formId)) {
                selectionMap.remove(ancestorFormId);
            }
        }

        return new HashSet<>(selectionMap.values());
    }

    public Observable<Set<RecordRef>> getSelectedRef() {
        return selectedRefs;
    }
}

