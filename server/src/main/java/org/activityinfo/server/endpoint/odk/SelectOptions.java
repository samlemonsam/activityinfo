package org.activityinfo.server.endpoint.odk;

import org.activityinfo.model.type.Cardinality;
import org.activityinfo.io.xform.form.Item;

import java.util.List;

public class SelectOptions {

    private Cardinality cardinality;
    private List<Item> items;

    public SelectOptions(Cardinality cardinality, List<Item> items) {
        this.cardinality = cardinality;
        this.items = items;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    public List<Item> getItems() {
        return items;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
