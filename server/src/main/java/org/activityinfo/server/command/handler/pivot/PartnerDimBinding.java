package org.activityinfo.server.command.handler.pivot;

import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.metadata.Activity;

import java.util.Arrays;
import java.util.List;


public class PartnerDimBinding extends DimBinding {

    private static final String PARTNER_ID_COLUMN = "PartnerId";
    private static final String PARTNER_LABEL_COLUMN = "PartnerLabel";

    private final Dimension model = new Dimension(DimensionType.Partner);

    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        return getColumnQuery(formTree.getRootFormId());
    }

    @Override
    public List<ColumnModel> getTargetColumnQuery(ResourceId targetFormId) {
        return getColumnQuery(targetFormId);
    }

    private List<ColumnModel> getColumnQuery(ResourceId formId) {

        SymbolExpr partnerField = new SymbolExpr(CuidAdapter.field(formId, CuidAdapter.PARTNER_FIELD));

        ColumnModel partnerId = new ColumnModel();
        partnerId.setExpression(partnerField);
        partnerId.setId(PARTNER_ID_COLUMN);

        ColumnModel partnerLabel = new ColumnModel();
        partnerLabel.setExpression(new CompoundExpr(partnerField, new SymbolExpr("label")));
        partnerLabel.setId(PARTNER_LABEL_COLUMN);

        return Arrays.asList(partnerId, partnerLabel);
    }

    @Override
    public Dimension getModel() {
        return model;
    }

    @Override
    public DimensionCategory[] extractCategories(Activity activity, ColumnSet columnSet) {
        return extractEntityCategories(columnSet, PARTNER_ID_COLUMN, PARTNER_LABEL_COLUMN);
    }

    @Override
    public DimensionCategory extractTargetCategory(Activity activity, ColumnSet columnSet, int rowIndex) {
        return extractEntityCategory(
                columnSet.getColumnView(PARTNER_ID_COLUMN), 
                columnSet.getColumnView(PARTNER_LABEL_COLUMN), rowIndex);
    }
}
