package org.activityinfo.model.analysis;


import com.google.common.base.Optional;
import org.activityinfo.model.resource.ResourceId;
import org.immutables.value.Value;


@Value.Immutable
public abstract class TableColumn {

    @Value.Default
    public String getId() {
        return ResourceId.generateCuid();
    }

    public abstract Optional<String> getLabel();

    public abstract String getFormula();
}
