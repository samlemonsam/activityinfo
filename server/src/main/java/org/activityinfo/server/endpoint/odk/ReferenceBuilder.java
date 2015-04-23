package org.activityinfo.server.endpoint.odk;

import com.google.common.collect.Iterables;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.io.xform.form.BindingType;
import org.activityinfo.io.xform.form.BodyElement;
import org.activityinfo.io.xform.form.Input;

import java.util.Set;

public class ReferenceBuilder implements OdkFormFieldBuilder {


    private final Set<ResourceId> range;

    public ReferenceBuilder(Set<ResourceId> range) {
        this.range = range;
    }

    @Override
    public BindingType getModelBindType() {
        return BindingType.STRING;
    }

    @Override
    public BodyElement createBodyElement(String ref, String label, String hint) {
        Input input = new Input();
        input.setRef(ref);
        input.setLabel(label);
        input.setHint(hint);
        input.setQuery(String.format("instance('%s')/root/item[]", Iterables.getOnlyElement(range).asString()));
        return input;
    }
}
