package org.activityinfo.ui.client.component.form.field.suggest;

import com.google.gwt.user.client.ui.SuggestOracle;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.FormInstanceLabeler;
import org.activityinfo.core.shared.importing.match.names.LatinPlaceNameScorer;

import java.util.ArrayList;
import java.util.List;

public class InstanceSuggestOracle extends SuggestOracle {

    private List<FormInstance> instances;
    private LatinPlaceNameScorer scorer = new LatinPlaceNameScorer();

    public InstanceSuggestOracle(List<FormInstance> range) {
        this.instances = range;
    }

    @Override
    public void requestSuggestions(Request request, Callback callback) {
        List<Suggestion> suggestions = new ArrayList<>();
        for(FormInstance instance : instances) {
            String label = FormInstanceLabeler.getLabel(instance);
            if(scorer.score(request.getQuery(), label) > 0.5) {
                suggestions.add(new InstanceSuggestion(instance));
            }
        }
        callback.onSuggestionsReady(request, new Response(suggestions));
    }
}
