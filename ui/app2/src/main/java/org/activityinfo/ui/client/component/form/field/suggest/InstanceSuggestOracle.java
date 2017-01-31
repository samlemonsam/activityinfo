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
