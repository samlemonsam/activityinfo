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
package org.activityinfo.ui.client.lookup.model;

import com.google.common.base.Optional;
import org.activityinfo.model.formTree.LookupKey;
import org.activityinfo.model.type.RecordRef;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

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
    private Set<RecordRef> initialSelection;
    private Map<LookupKey, String> selectedKeys;

    public LookupModel() {
        this.state = State.EMPTY;
        this.selectedKeys = Collections.emptyMap();
        this.initialSelection = Collections.emptySet();
    }

    public LookupModel(Set<RecordRef> initialSelection) {
        this.state = State.INITIAL;
        this.initialSelection = initialSelection;
        this.selectedKeys = Collections.emptyMap();
    }

    public LookupModel(RecordRef initialSelection) {
        this(Collections.singleton(initialSelection));
    }

    public LookupModel(Map<LookupKey, String> selectedKeys) {
        this.state = State.SELECTION;
        this.selectedKeys = selectedKeys;
        this.initialSelection = Collections.emptySet();
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

    public Set<RecordRef> getInitialSelection() {
        return initialSelection;
    }

    public Optional<String> getSelectedKey(LookupKey level) {
        return Optional.fromNullable(selectedKeys.get(level));
    }
}
