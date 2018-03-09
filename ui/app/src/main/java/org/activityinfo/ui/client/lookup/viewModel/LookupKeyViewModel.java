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
import org.activityinfo.observable.Observable;

import java.util.List;

public class LookupKeyViewModel {

    private LookupKey lookupKey;
    private final Observable<Boolean> enabled;
    private final Observable<Optional<String>> selectedKey;
    private final Observable<List<String>> choices;

    LookupKeyViewModel(LookupKey lookupKey,
                       Observable<Boolean> enabled,
                       Observable<Optional<String>> selectedKey,
                       Observable<List<String>> choices) {

        this.lookupKey = lookupKey;
        this.selectedKey = selectedKey;
        this.enabled = enabled;
        this.choices = choices;
    }

    public boolean isLeaf() {
        return lookupKey.getChildLevels().isEmpty();
    }

    public Observable<Optional<String>> getSelectedKey() {
        return selectedKey;
    }

    public Observable<Boolean> isEnabled() {
        return enabled;
    }

    public Observable<List<String>> getChoices() {
        return choices;
    }

    public String getKeyLabel() {
        return lookupKey.getKeyLabel();
    }

    public LookupKey getLookupKey() {
        return lookupKey;
    }
}
