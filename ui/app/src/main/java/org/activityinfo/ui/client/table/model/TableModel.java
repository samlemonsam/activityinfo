package org.activityinfo.ui.client.table.model;

import org.activityinfo.model.resource.ResourceId;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * The user's table model
 */
@Value.Immutable
public abstract class TableModel  {

    abstract ResourceId getFormId();

    abstract Optional<ResourceId> getSubFormId();

}
