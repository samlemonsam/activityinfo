package org.activityinfo.server.command.handler.pivot;

import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;

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
        partnerLabel.setExpression(new CompoundExpr(CuidAdapter.partnerField(activityId), "Label"));
        partnerLabel.setId(PARTNER_LABEL_COLUMN);
        
        return Arrays.asList(partnerId, partnerLabel);
    }

    @Override
    public Dimension getModel() {
        return model;
    }

    @Override
    public DimensionCategory[] extractCategories(ActivityMetadata activity, FormTree formTree, ColumnSet columnSet) {
        return extractEntityCategories(columnSet, PARTNER_ID_COLUMN, PARTNER_LABEL_COLUMN);
    }

}
