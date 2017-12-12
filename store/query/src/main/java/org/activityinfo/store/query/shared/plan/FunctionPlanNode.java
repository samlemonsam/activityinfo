package org.activityinfo.store.query.shared.plan;

import org.activityinfo.model.expr.functions.ExprFunction;
import org.activityinfo.model.type.FieldType;

import java.util.List;


public class FunctionPlanNode extends PlanNode {

    private ExprFunction function;
    private final List<PlanNode> argumentNodes;
    private final FieldType resultType;

    public FunctionPlanNode(ExprFunction function, List<PlanNode> argumentNodes, FieldType resultType) {
        this.function = function;
        this.argumentNodes = argumentNodes;
        this.resultType = resultType;
    }

    @Override
    public FieldType getFieldType() {
        return resultType;
    }

    @Override
    public <T> T accept(PlanVisitor<T> visitor) {
        return visitor.visitFunctionCall(this);
    }

    public ExprFunction getFunction() {
        return function;
    }

    public List<PlanNode> getArgumentNodes() {
        return argumentNodes;
    }
}
