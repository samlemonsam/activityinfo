package org.activityinfo.server.command.handler.pivot;

import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.content.EntityCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;

import java.util.Collections;
import java.util.List;

/**
 * Retrieves values for the dimension
 */
public abstract class DimBinding {
    
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        return Collections.emptyList();
    }

    public abstract Dimension getModel();
    
    public abstract DimensionCategory[] extractCategories(ActivityMetadata activity, FormTree formTree, ColumnSet columnSet);
    
    protected final int activityIdOf(FormTree formTree) {
        return CuidAdapter.getLegacyIdFromCuid(formTree.getRootFormClass().getId());
    }

    protected final DimensionCategory[] extractEntityCategories(ColumnSet columnSet, String idColumn, String labelColumn) {
        ColumnView id = columnSet.getColumnView(idColumn);
        ColumnView label = columnSet.getColumnView(labelColumn);

        int numRows = columnSet.getNumRows();

        DimensionCategory categories[] = new DimensionCategory[numRows];

        for (int i = 0; i < numRows; i++) {
            String partnerId = id.getString(i);
            categories[i] = new EntityCategory(CuidAdapter.getLegacyIdFromCuid(partnerId), label.getString(i));
        }

        return categories;
    }
}
