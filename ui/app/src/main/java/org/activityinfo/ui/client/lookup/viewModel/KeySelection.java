package org.activityinfo.ui.client.lookup.viewModel;


import com.google.common.base.Optional;
import org.activityinfo.model.formTree.LookupKey;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;

import java.util.*;

class KeySelection {

    private static final BitSet EMPTY_SET = new BitSet();

    /**
     * For each lookup key, a bit set indicating which rows in the key matrix
     * match the selected key value.
     */
    private final Map<LookupKey, Observable<BitSet>> matchingRows;

    private final Map<LookupKey, Observable<List<String>>> choices;

    private final Map<LookupKey, Observable<Optional<String>>> selectedKeys;

    private final Observable<Optional<RecordRef>> selectedRef;

    KeySelection(KeyMatrix keyMatrix, Map<LookupKey, Observable<Optional<String>>> selectedKeys) {
        this.selectedKeys = selectedKeys;

        /*
         * Construct a bitset of the rows that match the selected key at each level
         */
        matchingRows = new HashMap<>();
        for (LookupKey lookupKey : selectedKeys.keySet()) {
            Observable<Optional<String>> selectedKey = selectedKeys.get(lookupKey);
            Observable<BitSet> matching = keyMatrix.getMatchingRows(lookupKey, selectedKey);
            matchingRows.put(lookupKey, matching);
        }

        /*
         * For each level, find the choices as a function of the parent choices.
         */
        choices = new HashMap<>();
        for (LookupKey lookupKey : selectedKeys.keySet()) {
            if (lookupKey.isRoot()) {
                choices.put(lookupKey, keyMatrix.getDistinctKeys(lookupKey));
            } else {
                choices.put(lookupKey, keyMatrix.getDistinctKeys(lookupKey, matchingRows(lookupKey.getParentLevels())));
            }
        }

        /*
         * Find id of the selected row based on the intersection of all keys.
         */
        selectedRef = keyMatrix.getMatchingRecordRef(matchingRows(selectedKeys.keySet()));
    }

    /**
     * Computes the BitSet of the rows that match all of the given {@code keys}.
     */
    private Observable<BitSet> matchingRows(Iterable<LookupKey> keys) {
        List<Observable<BitSet>> parents = new ArrayList<>();
        for (LookupKey key : keys) {
            parents.add(matchingRows.get(key));
        }
        return Observable.flatten(parents).transform(this::intersect);
    }

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