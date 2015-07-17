package org.activityinfo.server.endpoint.odk;

import org.activityinfo.model.type.Cardinality;
import org.activityinfo.io.xform.form.*;

import java.util.List;

class SelectBuilder implements OdkFormFieldBuilder {
    final private BindingType modelBindType;
    final private Cardinality cardinality;
    final private List<Item> items;

    SelectBuilder(BindingType modelBindType, SelectOptions selectOptions) {
        this.modelBindType = modelBindType;
        this.cardinality = selectOptions.getCardinality();
        this.items = selectOptions.getItems();
    }

    @Override
    public BindingType getModelBindType() {
        return modelBindType;
    }

    @Override
    public SelectElement createBodyElement(String ref, String label, String hint) {
        SelectElement select;
        switch(cardinality) {
            case SINGLE:
                select = new Select1();
                break;
            case MULTIPLE:
                select = new Select();
                break;
            default:
                throw new IllegalStateException("Cardinality: " + cardinality);
        }

        select.setRef(ref);
        select.setLabel(label);
        select.getItems().addAll(items);
        select.setHint(hint);
        return select;
    }
}
