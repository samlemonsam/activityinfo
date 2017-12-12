package org.activityinfo.store.query.shared.plan;

import com.google.common.collect.Lists;
import org.activityinfo.model.expr.diagnostic.ExprException;
import org.activityinfo.model.expr.functions.ColumnFunction;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.EmptyColumnView;
import org.activityinfo.store.query.shared.FilterLevel;
import org.activityinfo.store.query.shared.FormScanBatch;
import org.activityinfo.store.query.shared.Slot;
import org.activityinfo.store.query.shared.join.JoinedReferenceColumnViewSlot;
import org.activityinfo.store.query.shared.join.ReferenceJoin;

import java.util.ArrayList;
import java.util.List;

/**
 * Schedules a query plan.
 */
public class QueryScheduler implements PlanVisitor<Slot<ColumnView>> {

    private FormTree formTree;
    private FilterLevel filterLevel;
    private FormScanBatch batch;

    public QueryScheduler(FormTree formTree, FilterLevel filterLevel, FormScanBatch batch) {
        this.formTree = formTree;
        this.filterLevel = filterLevel;
        this.batch = batch;
    }

    @Override
    public Slot<ColumnView> visitConstant(ConstantPlanNode constantPlanNode) {
        return batch.addConstantColumn(filterLevel, formTree.getRootFormClass(), constantPlanNode.getValue());
    }

    @Override
    public Slot<ColumnView> visitErrorNode(ErrorNode errorNode) {
        return batch.addConstantColumn(filterLevel, formTree.getRootFormClass(), (String)null);
    }

    @Override
    public Slot<ColumnView> visitFunctionCall(FunctionPlanNode functionPlanNode) {
        if(functionPlanNode.getFunction() instanceof ColumnFunction) {
            return scheduleVectorizedCall(functionPlanNode);
        } else {
            throw new UnsupportedOperationException("TODO: " + functionPlanNode.getFunction().getId());
        }
    }

    @Override
    public Slot<ColumnView> visitFieldNode(FieldPlanNode fieldPlanNode) {
        return batch.addLeafColumn(filterLevel, fieldPlanNode.getFormId(), fieldPlanNode.getExpr());
    }

    @Override
    public Slot<ColumnView> visitRecordId(RecordIdNode recordIdNode) {
        return batch.addRecordId(filterLevel, recordIdNode.getFormId());
    }

    @Override
    public Slot<ColumnView> visitFormId(FormIdNode formIdNode) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Slot<ColumnView> visitReference(ReferencePlanNode referenceNode) {

        List<ReferenceJoin> links = new ArrayList<>();
        PlanNode leafNode = referenceNode;
        while(leafNode instanceof ReferencePlanNode) {
            ReferencePlanNode refNode = (ReferencePlanNode) leafNode;
            links.add(batch.addJoinLink(filterLevel, refNode));
            leafNode = refNode.getRightNode();
        }

        Slot<ColumnView> leafSlot = leafNode.accept(this);

        return new JoinedReferenceColumnViewSlot(links, leafSlot);
    }

    @Override
    public Slot<ColumnView> visitSubForm(SubFormNode subFormNode) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Slot<ColumnView> visitEnumItem(EnumNode enumNode) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Slot<ColumnView> visitAggregate(AggregateNode aggregateNode) {
        throw new UnsupportedOperationException("TODO");
    }

    private Slot<ColumnView> scheduleVectorizedCall(final FunctionPlanNode node) {

        final List<Slot<ColumnView>> argumentSlots = Lists.newArrayList();

        for (PlanNode argumentNode : node.getArgumentNodes()) {
            argumentSlots.add(argumentNode.accept(this));
        }

        return new Slot<ColumnView>() {
            @Override
            public ColumnView get() {
                List<ColumnView> arguments = Lists.newArrayList();
                for (Slot<ColumnView> argument : argumentSlots) {
                    ColumnView view = argument.get();
                    if(view == null) {
                        throw new IllegalStateException();
                    }
                    arguments.add(view);
                }
                try {
                    return ((ColumnFunction) node.getFunction()).columnApply(arguments.get(0).numRows(), arguments);
                } catch (ExprException e) {
                    int numRows = arguments.get(0).numRows();
                    return new EmptyColumnView(ColumnType.STRING, numRows);
                }
            }
        };
    }
}
