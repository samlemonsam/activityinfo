package org.activityinfo.server.endpoint.odk;

import com.google.common.collect.Iterables;
import org.activityinfo.io.xform.form.BindingType;
import org.activityinfo.io.xform.form.BodyElement;
import org.activityinfo.io.xform.form.Input;
import org.activityinfo.model.resource.ResourceId;

import java.util.Collection;

public class ReferenceBuilder implements OdkFormFieldBuilder {


    private final Collection<ResourceId> range;

    public ReferenceBuilder(Collection<ResourceId> range) {
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
