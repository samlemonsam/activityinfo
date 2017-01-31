package org.activityinfo.ui.client.component.importDialog.model;

import org.activityinfo.ui.client.component.importDialog.model.strategy.ImportTarget;

/**
 * ColumnAction which maps the input SourceColumn to an existing Field
 */
public class MapExistingAction extends ColumnAction {
    private ImportTarget target;

    public MapExistingAction(ImportTarget target) {
        this.target = target;
    }

    public ImportTarget getTarget() {
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MapExistingAction that = (MapExistingAction) o;

        return !(target != null ? !target.equals(that.target) : that.target != null);

    }

    @Override
    public int hashCode() {
        return target != null ? target.hashCode() : 0;
    }
}
