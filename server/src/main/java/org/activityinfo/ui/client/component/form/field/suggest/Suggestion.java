package org.activityinfo.ui.client.component.form.field.suggest;

import com.google.gwt.user.client.ui.SuggestOracle;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.FormInstanceLabeler;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.enumerated.EnumItem;

public class Suggestion implements SuggestOracle.Suggestion {

    private final String label;
    private final ResourceId id;

    public Suggestion(String label, ResourceId id) {
        this.label = label;
        this.id = id;
    }

    public Suggestion(FormInstance instance) {
        this.label = FormInstanceLabeler.getLabel(instance);
        this.id = instance.getId();
    }

    public Suggestion(EnumItem item) {
        this.label = item.getLabel();
        this.id = item.getId();
    }

    @Override
    public String getDisplayString() {
        return getReplacementString();
    }

    @Override
    public String getReplacementString() {
        return label;
    }

    public ResourceId getId() {
        return id;
    }
}
