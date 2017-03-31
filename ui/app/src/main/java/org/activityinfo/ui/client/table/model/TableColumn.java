package org.activityinfo.ui.client.table.model;


import org.activityinfo.model.resource.ResourceId;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public abstract class TableColumn {

    @Value.Default
    public String getId() {
        return ResourceId.generateCuid();
    }

    public abstract Optional<String> getLabel();

    public abstract String getFormula();
}
