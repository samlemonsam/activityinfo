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
package org.activityinfo.ui.client.component.form.field.suggest;

import com.google.gwt.user.client.ui.SuggestOracle;
import org.activityinfo.model.form.TypedFormRecord;
import org.activityinfo.model.form.FormInstanceLabeler;
import org.activityinfo.model.type.RecordRef;

public class ReferenceSuggestion implements SuggestOracle.Suggestion {

    private final String label;
    private final RecordRef ref;

    public ReferenceSuggestion(String label, RecordRef ref) {
        this.label = label;
        this.ref = ref;
    }

    public ReferenceSuggestion(TypedFormRecord instance) {
        this.label = FormInstanceLabeler.getLabel(instance);
        this.ref = new RecordRef(instance.getFormId(), instance.getId());
    }

    @Override
    public String getDisplayString() {
        return getReplacementString();
    }

    @Override
    public String getReplacementString() {
        return label;
    }

    public RecordRef getRef() {
        return ref;
    }
}
