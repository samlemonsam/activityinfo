package org.activityinfo.ui.client.measureDialog.view;

import com.google.gwt.resources.client.ImageResource;
import org.activityinfo.model.analysis.TableColumn;
import org.activityinfo.ui.client.analysis.model.MeasureModel;
import org.activityinfo.ui.client.table.ColumnDialog;

import java.util.Collection;
import java.util.Optional;


public abstract class MeasureTreeNode {

    public abstract String getId();

    public abstract String getLabel();

    public abstract ImageResource getIcon();

    public abstract MeasureModel newMeasure();

    public Optional<TableColumn> newTableColumn() {
        return Optional.empty();
    }
}
