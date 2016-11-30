package org.activityinfo.ui.client.component.form.field.suggest;

import com.google.gwt.user.client.ui.SuggestOracle;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.FormInstanceLabeler;
import org.activityinfo.model.type.RecordRef;

public class ReferenceSuggestion implements SuggestOracle.Suggestion {

    private final String label;
    private final RecordRef ref;

    public ReferenceSuggestion(String label, RecordRef ref) {
        this.label = label;
        this.ref = ref;
    }

    public ReferenceSuggestion(FormInstance instance) {
        this.label = FormInstanceLabeler.getLabel(instance);
        this.ref = new RecordRef(instance.getClassId(), instance.getId());
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
