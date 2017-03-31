package org.activityinfo.ui.client.table.model;

import org.activityinfo.model.resource.ResourceId;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

/**
 * The user's table model
 */
@Value.Immutable
public abstract class TableModel  {

    public abstract ResourceId getFormId();

    public abstract List<TableColumn> getColumns();

    public abstract Optional<ResourceId> getSubFormId();

}
