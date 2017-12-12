package org.activityinfo.store.query.shared.plan;

import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.primitive.TextType;

public class ErrorNode extends PlanNode {

    public ErrorNode() {
    }

    public ErrorNode(ExprNode symbolExpr, String message) {

    }

    @Override
    public FieldType getFieldType() {
        return TextType.SIMPLE;
    }

    @Override
    public <T> T accept(PlanVisitor<T> visitor) {
        return visitor.visitErrorNode(this);
    }


}
