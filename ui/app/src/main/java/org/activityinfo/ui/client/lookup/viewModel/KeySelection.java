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
import org.activityinfo.model.formTree.LookupKey;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;

import java.util.*;

/**
 *
 */
class KeySelection {

    private static final BitSet EMPTY_SET = new BitSet();

    /**
     * For each lookup key, a bit set indicating which rows in the key matrix
     * match the selected key value.
     */
    private final Map<LookupKey, Observable<BitSet>> matchingRows;

    private final Map<LookupKey, Observable<List<String>>> choices;

    private final KeyMatrix keyMatrix;
    private final Map<LookupKey, Observable<Optional<String>>> selectedKeys;

    private final Observable<Optional<RecordRef>> selectedRef;

    KeySelection(KeyMatrix keyMatrix, Map<LookupKey,Observable<Optional<String>>> selectedKeys) {
        this.keyMatrix = keyMatrix;
        this.selectedKeys = selectedKeys;

        /*
         * Construct a bitset of the rows that match the selected key at each level
         */
        matchingRows = new HashMap<>();
        for (LookupKey lookupKey : selectedKeys.keySet()) {
            if(keyMatrix.containsLookupKey(lookupKey)) {
                Observable<Optional<String>> selectedKey = selectedKeys.get(lookupKey);
                Observable<BitSet> matching = keyMatrix.getMatchingRows(lookupKey, selectedKey);
                matchingRows.put(lookupKey, matching);
            }
        }

        /*
         * For each level, find the choices as a function of the parent choices.
         */
        choices = new HashMap<>();
        for (LookupKey lookupKey : selectedKeys.keySet()) {
            if(keyMatrix.containsLookupKey(lookupKey)) {
                if (lookupKey.isRoot()) {
                    choices.put(lookupKey, keyMatrix.getDistinctKeys(lookupKey));
                } else {
                    choices.put(lookupKey, keyMatrix.getDistinctKeys(lookupKey, matchingRows(lookupKey.getParentLevels())));
                }
            }
        }

        /*
         * Find id of the selected row based on the intersection of all keys.
         */
        selectedRef = keyMatrix.getMatchingRecordRef(matchingRows(selectedKeys.keySet()));
    }

    public ResourceId getFormId() {
        return keyMatrix.getFormId();
    }

    public boolean containsLookupKey(LookupKey lookupKey) {
        return keyMatrix.containsLookupKey(lookupKey);
    }

    /**
     * Computes the BitSet of the rows that match all of the given {@code keys}.
     */
    private Observable<BitSet> matchingRows(Iterable<LookupKey> keys) {
        List<Observable<BitSet>> parents = new ArrayList<>();
        for (LookupKey key : keys) {
            if (selectedKeys.containsKey(key) && matchingRows.containsKey(key)) {
                parents.add(Observable.join(selectedKeys.get(key),
                                            matchingRows.get(key),
                                            keyMatrix.getKeyColumnSize(key),
                                            this::effectiveBitSet));
            }
        }
        return Observable.flatten(parents).transform(this::intersect);
    }

    private Observable<BitSet> effectiveBitSet(Optional<String> selected, BitSet bitSet, Integer size) {
        if (selected.isPresent()) {
            // If the key has been selected, then we return its bitset
            return Observable.just(bitSet);
        } else {
            // Otherwise we must return a "full" bitset in order to find the correct intersection of bitsets
            return Observable.just(fullBitSet(size));
        }
    }

    private static BitSet fullBitSet(int size) {
        if (size <= 0) {
            return EMPTY_SET;
        }
        BitSet fullBitSet = new BitSet(size);
        fullBitSet.set(0, size-1, true);
        return fullBitSet;
    }

    public Observable<Optional<RecordRef>> getSelectedRef() {
        return selectedRef;
    }

    /**
     * Finds the intersection between zero or more BitSets. If there are no bitsets,
     * the result is the {@link #EMPTY_SET}.
     */
    private BitSet intersect(List<BitSet> bitSets) {
        if(bitSets.size() == 1) {
            return bitSets.get(0);
        } else if(isEmpty(bitSets)) {
            return EMPTY_SET;
        } else {
            BitSet intersection = (BitSet) bitSets.get(0).clone();
            for (int i = 1; i < bitSets.size(); i++) {
                intersection.and(bitSets.get(i));
            }
            return intersection;
        }
    }

    private boolean isEmpty(List<BitSet> bitSets) {
        if(bitSets.isEmpty()) {
            return true;
        }
        for (BitSet set : bitSets) {
            if(set.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public Observable<List<String>> getChoices(LookupKey lookupKey) {
        return choices.get(lookupKey);
    }

}