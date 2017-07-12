package org.activityinfo.ui.client.measureDialog.view;

import com.google.gwt.resources.client.ImageResource;
import org.activityinfo.model.analysis.ImmutableTableColumn;
import org.activityinfo.model.analysis.TableColumn;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.analysis.model.ImmutableMeasureModel;
import org.activityinfo.ui.client.analysis.model.MeasureModel;
import org.activityinfo.ui.client.icons.IconBundle;

import java.util.Optional;

/**
 * Measure based on a quantity
 */
public class QuantityNode extends MeasureTreeNode {

    private ResourceId formId;
    private FormField field;

    public QuantityNode(ResourceId formId, FormField field) {
        this.formId = formId;
        this.field = field;
    }

    @Override
    public String getId() {
        return formId.asString() + "." + field.getId().asString();
    }

    @Override
    public String getLabel() {
        return field.getLabel();
    }

    @Override
    public ImageResource getIcon() {
        return IconBundle.iconForField(field.getType());
    }

    @Override
    public MeasureModel newMeasure() {
        return ImmutableMeasureModel.builder()
                .label(getLabel())
                .formId(formId)
                .formula(new SymbolExpr(field.getId()).asExpression())
                .build();
    }

    @Override
    public Optional<TableColumn> newTableColumn() {
        return Optional.of(
            ImmutableTableColumn.builder()
            .label(getLabel())
            .formula(field.getId().asString())
            .build());
    }
}
