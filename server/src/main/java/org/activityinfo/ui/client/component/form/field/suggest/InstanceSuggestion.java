package org.activityinfo.ui.client.component.form.field.suggest;

import com.google.gwt.user.client.ui.SuggestOracle;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.FormInstanceLabeler;

public class InstanceSuggestion implements SuggestOracle.Suggestion {

    private final FormInstance instance;

    public InstanceSuggestion(FormInstance instance) {
        this.instance = instance;
    }

    @Override
    public String getDisplayString() {
        return getReplacementString();
    }

    @Override
    public String getReplacementString() {
        return FormInstanceLabeler.getLabel(instance);
    }

    public ResourceId getInstanceId() {
        return instance.getId();
    }
}
