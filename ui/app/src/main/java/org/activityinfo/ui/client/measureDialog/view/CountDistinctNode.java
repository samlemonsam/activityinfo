package org.activityinfo.ui.client.measureDialog.view;

import com.google.gwt.resources.client.ImageResource;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formula.CompoundExpr;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.analysis.model.ImmutableMeasureModel;
import org.activityinfo.ui.client.analysis.model.MeasureModel;
import org.activityinfo.ui.client.analysis.model.Statistic;
import org.activityinfo.ui.client.icons.IconBundle;

public class CountDistinctNode extends MeasureTreeNode {

    private ResourceId rootFormId;
    private FormClass formClass;

    public CountDistinctNode(ResourceId rootFormId, FormClass formClass) {
        this.rootFormId = rootFormId;
        this.formClass = formClass;
    }

    @Override
    public String getId() {
        return formClass.getId().toString() + ":distinct";
    }

    @Override
    public String getLabel() {
        return I18N.MESSAGES.countDistinctMeasure(formClass.getLabel());
    }

    @Override
    public ImageResource getIcon() {
        return IconBundle.INSTANCE.count();
    }

    @Override
    public MeasureModel newMeasure() {
        return ImmutableMeasureModel.builder()
            .label(I18N.MESSAGES.countDistinctMeasure(formClass.getLabel()))
            .formId(rootFormId)
            .formula(new CompoundExpr(formClass.getId(), ColumnModel.ID_SYMBOL).asExpression())
            .addStatistics(Statistic.COUNT_DISTINCT)
            .build();
    }
}
