package org.activityinfo.server.command.handler.binding.dim;

import com.extjs.gxt.ui.client.data.BaseModelData;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.model.PartnerDTO;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.metadata.Activity;

import java.util.Arrays;
import java.util.List;

public class PartnerDimBinding extends DimBinding {

    private static final String PARTNER_ID_COLUMN = "PartnerId";
    private static final String PARTNER_LABEL_COLUMN = "PartnerLabel";

    private static final String PARTNER_FIELD = "partner";

    private final Dimension model = new Dimension(DimensionType.Partner);

    @Override
    public BaseModelData[] extractFieldData(BaseModelData[] dataArray, ColumnSet columnSet) {
        ColumnView id = columnSet.getColumnView(PARTNER_ID_COLUMN);
        ColumnView label = columnSet.getColumnView(PARTNER_LABEL_COLUMN);

        for (int i=0; i<columnSet.getNumRows(); i++) {
            String partnerId = id.getString(i);
            String partnerLabel = label.getString(i);

            PartnerDTO partner = new PartnerDTO(CuidAdapter.getLegacyIdFromCuid(partnerId), partnerLabel);

            dataArray[i].set(PARTNER_FIELD, partner);
        }

        return dataArray;
    }

    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {

        // Partner field is always located on the site form
        // for monthly reports
        if(formTree.getRootFormId().getDomain() == CuidAdapter.MONTHLY_REPORT_FORM_CLASS) {
            int activityId = CuidAdapter.getLegacyIdFromCuid(formTree.getRootFormId());
            ResourceId siteFormId = CuidAdapter.activityFormClass(activityId);
            return getColumnQuery(siteFormId);
        }

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
