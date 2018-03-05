package org.activityinfo.ui.client.measureDialog.view;

import com.google.gwt.resources.client.ImageResource;
import org.activityinfo.model.analysis.ImmutableTableColumn;
import org.activityinfo.model.analysis.TableColumn;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.ui.client.analysis.model.ImmutableMeasureModel;
import org.activityinfo.ui.client.analysis.model.MeasureModel;
import org.activityinfo.ui.client.icons.IconBundle;

import java.util.Optional;

public class IdNode extends MeasureTreeNode {

    private FormClass formClass;
    private ExprNode formula;

    public IdNode(FormClass formClass) {
        this.formClass = formClass;
        this.formula = new SymbolExpr(ColumnModel.ID_SYMBOL);
    }

    @Override
    public String getId() {
        return ColumnModel.ID_SYMBOL;
    }

    @Override
    public String getLabel() {
        return "Record ID";
    }

    @Override
    public ImageResource getIcon() {
        return IconBundle.INSTANCE.textField();
    }

    @Override
    public MeasureModel newMeasure() {
        return ImmutableMeasureModel.builder()
                .label(getLabel())
                .formId(formClass.getId())
                .formula(formula.asExpression())
                .build();
    }

    @Override
    public Optional<TableColumn> newTableColumn() {
        return Optional.of(ImmutableTableColumn.builder()
            .label(getLabel())
            .formula(formula.asExpression())
            .build());
    }
}
