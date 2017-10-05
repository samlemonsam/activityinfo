package org.activityinfo.ui.client.lookup.model;

import com.google.common.base.Optional;
import org.activityinfo.model.formTree.LookupKey;
import org.activityinfo.model.type.RecordRef;

import java.util.Collections;
import java.util.Map;

/**
 * Models the interactive, hierarchical lookup of a reference value.
 *
 * <p>Users look up individual records with a set of hierarchical keys. The lookup process
 * can be in one of three states: EMPTY, INITIAL, or SELECTION.
 */
public class LookupModel {

    public enum State {
        /**
         * There is no initial selection, and the user has not made any
         * selection yet.
         */
        EMPTY,

        /**
         * There is an initial record selection, and the user has not made any
         * explicit change yet.
         */
        INITIAL,

        /**
         * The user has explicitly selected a key at some level.
         */
        SELECTION
    }

    private State state;
    private RecordRef initialSelection;
    private Map<LookupKey, String> selectedKeys;

    public LookupModel() {
        this.state = State.EMPTY;
        this.selectedKeys = Collections.emptyMap();
    }

    public LookupModel(RecordRef initialSelection) {
        this.state = State.INITIAL;
        this.initialSelection = initialSelection;
        this.selectedKeys = Collections.emptyMap();
    }

    public LookupModel(Map<LookupKey, String> selectedKeys) {
        this.state = State.SELECTION;
        this.selectedKeys = selectedKeys;
    }

    public State getState() {
        return state;
    }

    public Map<LookupKey, String> getSelectedKeys() {
        switch (state) {
            case SELECTION:
                return selectedKeys;
            default:
                return Collections.emptyMap();
        }
    }

    public Optional<RecordRef> getInitialSelection() {
        return Optional.fromNullable(initialSelection);
    }

    public Optional<String> getSelectedKey(LookupKey level) {
        return Optional.fromNullable(selectedKeys.get(level));
    }
}
