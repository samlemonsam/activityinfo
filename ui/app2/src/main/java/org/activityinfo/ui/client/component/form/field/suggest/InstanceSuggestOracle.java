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
import org.activityinfo.io.match.names.LatinPlaceNameScorer;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.component.form.field.OptionSet;

import java.util.ArrayList;
import java.util.List;

public class InstanceSuggestOracle extends SuggestOracle {

    private ResourceId formId;
    private OptionSet options;
    private LatinPlaceNameScorer scorer = new LatinPlaceNameScorer();

    public InstanceSuggestOracle(ResourceId formId, OptionSet options) {
        this.formId = formId;
        this.options = options;
    }

    @Override
    public void requestSuggestions(Request request, Callback callback) {
        List<Suggestion> suggestions = new ArrayList<>();
        for (int i = 0; i < options.getCount(); i++) {
            String label = options.getLabel(i);
            if (scorer.score(request.getQuery(), label) > 0.5) {
                suggestions.add(new ReferenceSuggestion(
                        options.getLabel(i),
                        options.getRef(i)));
            }
        }

        // if scorer didn't give any results try to iterate with "contains"
        if (suggestions.isEmpty()) {
            for (int i = 0; i < options.getCount(); i++) {
                String label = options.getLabel(i);
                if (request.getQuery() != null && label.toUpperCase().contains(request.getQuery().toUpperCase())) {
                    suggestions.add(new ReferenceSuggestion(
                            options.getLabel(i),
                            options.getRef(i)));
                }
            }
        }
        callback.onSuggestionsReady(request, new Response(suggestions));
    }
}
