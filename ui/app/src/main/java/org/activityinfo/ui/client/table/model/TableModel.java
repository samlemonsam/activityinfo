package org.activityinfo.ui.client.table.model;

import org.activityinfo.model.resource.ResourceId;
import org.immutables.value.Value;

/**
 * The user's table model
 */
@Value.Immutable
public abstract class TableModel  {

    abstract ResourceId getFormId();

}
