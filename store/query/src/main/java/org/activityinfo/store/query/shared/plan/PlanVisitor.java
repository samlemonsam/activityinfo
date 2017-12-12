package org.activityinfo.store.query.shared.plan;

public interface PlanVisitor<T> {

    T visitAggregate(AggregateNode aggregateNode);

    T visitConstant(ConstantPlanNode constantPlanNode);

    T visitErrorNode(ErrorNode errorNode);

    T visitFunctionCall(FunctionPlanNode functionPlanNode);

    T visitFieldNode(FieldPlanNode fieldPlanNode);

    T visitRecordId(RecordIdNode recordIdNode);

    T visitFormId(FormIdNode formIdNode);

    T visitReference(ReferencePlanNode referenceNode);

    T visitSubForm(SubFormNode subFormNode);

    T visitEnumItem(EnumNode enumNode);
}
