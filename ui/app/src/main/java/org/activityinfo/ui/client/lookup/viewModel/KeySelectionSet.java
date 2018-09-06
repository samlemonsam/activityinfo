/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.ui.client.lookup.viewModel;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.activityinfo.model.formTree.LookupKey;
import org.activityinfo.model.formTree.LookupKeySet;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;

import java.util.*;
import java.util.stream.Collectors;

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
        if (key.isRoot()) {
            return Observable.just(true);
        } else {
            // First need to get the status of every parent of this key
            List<Observable<Boolean>> parentKeyStatus = key.getParentLevels().stream()
                    .map(this::getSelectedKey)
                    .map(potentialParentKey -> potentialParentKey.transform(Optional::isPresent))
                    .collect(Collectors.toList());

            // Then we flatten the list of observables and reduce the status of all the parent keys to whether the
            // current key is enabled (which is true if *any* parents are enabled)
            return Observable.flatten(parentKeyStatus)
                    .transform(statusList -> statusList.stream().reduce(false, (a, b) -> a || b));
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

