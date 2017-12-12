package org.activityinfo.store.query.shared.plan;

import org.activityinfo.model.expr.ConstantExpr;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;

public class ConstantPlanNode extends PlanNode {

    private final ConstantExpr expr;

    public ConstantPlanNode(ConstantExpr expr) {
        this.expr = expr;
    }

    public ConstantPlanNode(String expr) {
        this(new ConstantExpr(expr));
    }

    @Override
    public FieldType getFieldType() {
        return expr.getType();
    }

    @Override
    public <T> T accept(PlanVisitor<T> visitor) {
        return visitor.visitConstant(this);
    }

    public FieldValue getValue() {
        return expr.getValue();
    }
}
