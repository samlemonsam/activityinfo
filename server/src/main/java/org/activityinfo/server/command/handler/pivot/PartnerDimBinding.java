package org.activityinfo.server.command.handler.pivot;

import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.content.EntityCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;

import java.util.Arrays;
import java.util.List;


public class PartnerDimBinding extends DimBinding {

    private static final String PARTNER_ID_COLUMN = "PartnerId";
    private static final String PARTNER_LABEL_COLUMN = "PartnerLabel";

    private final Dimension model = new Dimension(DimensionType.Partner);

    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
    
        int activityId = activityIdOf(formTree);

        ColumnModel partnerId = new ColumnModel();
        partnerId.setExpression(CuidAdapter.partnerField(activityId));
        partnerId.setId(PARTNER_ID_COLUMN);
        
        ColumnModel partnerLabel = new ColumnModel();
        partnerLabel.setExpression(CuidAdapter.partnerField(activityId).asString() + ".Label");
        partnerLabel.setId(PARTNER_LABEL_COLUMN);
        
        return Arrays.asList(partnerId, partnerLabel);
    }

    @Override
    public Dimension getModel() {
        return model;
    }

    @Override
    public DimensionCategory[] extractCategories(FormTree formTree, ColumnSet columnSet) {

        ColumnView id = columnSet.getColumnView(PARTNER_ID_COLUMN);
        ColumnView label = columnSet.getColumnView(PARTNER_LABEL_COLUMN);

        int numRows = columnSet.getNumRows();
        
        DimensionCategory categories[] = new DimensionCategory[numRows];
        
        for (int i = 0; i < numRows; i++) {
            String partnerId = id.getString(i);
            categories[i] = new EntityCategory(CuidAdapter.getLegacyIdFromCuid(partnerId), label.getString(i));
        }

        return categories;
    }

}
