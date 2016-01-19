package org.activityinfo.server.command.handler.pivot;


import com.google.api.client.repackaged.com.google.common.base.Strings;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.content.EntityCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;

import java.util.Arrays;
import java.util.List;

public class SiteDimBinding extends DimBinding {

    private static final String ID_COLUMN = "SiteId";
    
    private static final String LABEL_COLUMN = "SiteName";
    
    private final Dimension model = new Dimension(DimensionType.Site);
    
    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        
        // Sites don't actually have their own label, so we will use
        // the closest thing to a unique label for sites, which is their
        // location name
        
        return Arrays.asList(
                new ColumnModel().setExpression(ColumnModel.ID_SYMBOL).as(ID_COLUMN),
                new ColumnModel().setExpression(
                        new CompoundExpr(
                                CuidAdapter.locationField(activityIdOf(formTree)),
                                "label"))
                        .as(LABEL_COLUMN));
    }

    @Override
    public Dimension getModel() {
        return model;
    }

    @Override
    public DimensionCategory[] extractCategories(ActivityMetadata activity, FormTree formTree, ColumnSet columnSet) {
        ColumnView id = columnSet.getColumnView(ID_COLUMN);
        ColumnView label = columnSet.getColumnView(LABEL_COLUMN);

        int numRows = columnSet.getNumRows();

        DimensionCategory categories[] = new DimensionCategory[numRows];

        for (int i = 0; i < numRows; i++) {
            String idString = id.getString(i);
            String labelString = label.getString(i);
            
            // Note that we try to gracefully handle an empty location label because not all forms
            // will have a location in the new data model
            // Legacy activities with "nullary" location types, when represented in the new data model,
            // have *no* location field, so we just have to treat it as a blank.
            
            categories[i] = new EntityCategory(CuidAdapter.getLegacyIdFromCuid(idString),
                    Strings.nullToEmpty(labelString));
        }

        return categories;    
    }
}
