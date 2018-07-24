package org.activityinfo.store.query.shared.plan;

import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.formula.*;
import org.activityinfo.model.formula.functions.CoalesceFunction;
import org.activityinfo.model.formula.functions.ColumnFunction;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.query.RowSource;
import org.activityinfo.store.query.shared.NodeMatch;
import org.activityinfo.store.query.shared.NodeMatcher;
import org.activityinfo.store.spi.FormStorageProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QueryPlanBuilder {

    private FormStorageProvider provider;
    private FormTreeBuilder formTreeBuilder;

    public QueryPlanBuilder(FormStorageProvider provider) {
        this.provider = provider;
        this.formTreeBuilder = new FormTreeBuilder(provider);
    }

    public QueryPlan build(QueryModel model) {

        RowSource rowSource = model.getRowSources().get(0);
        FormTree formTree = formTreeBuilder.queryTree(rowSource.getRootFormId());

        List<ColumnPlanNode> columns = new ArrayList<>();
        for (ColumnModel columnModel : model.getColumns()) {
            columns.add(parse(formTree, columnModel));
        }
        return new QueryPlan(new ProjectionNode(columns));
    }

    private ColumnPlanNode parse(FormTree formTree, ColumnModel columnModel) {
        FormulaNode formula = columnModel.getFormula();
        return formula.accept(new FormulaVisitor<ColumnPlanNode>() {
            @Override
            public ColumnPlanNode visitConstant(ConstantNode node) {
                return new ConstantColumn(node.getValue(), new CountRecordsOp(formTree.getRootFormId()));
            }

            @Override
            public ColumnPlanNode visitGroup(GroupNode expr) {
                return expr.accept(this);
            }

            @Override
            public ColumnPlanNode visitFunctionCall(FunctionCallNode callNode) {
                if(!(callNode.getFunction() instanceof ColumnFunction)) {
                    throw new UnsupportedOperationException("TODO: " + callNode.getFunction().getId());
                }
                List<ColumnPlanNode> arguments = new ArrayList<>();
                for (FormulaNode formulaNode : callNode.getArguments()) {
                    arguments.add(formulaNode.accept(this));
                }
                return new VectorOp((ColumnFunction) callNode.getFunction(), arguments);
            }

            @Override
            public ColumnPlanNode visitSymbol(SymbolNode symbolNode) {
                NodeMatcher nodeMatcher = new NodeMatcher(formTree);
                Collection<NodeMatch> nodeMatches = nodeMatcher.resolveSymbol(symbolNode);

                return plan(nodeMatches);
            }


            @Override
            public ColumnPlanNode visitCompoundExpr(CompoundExpr compoundExpr) {
                NodeMatcher nodeMatcher = new NodeMatcher(formTree);
                Collection<NodeMatch> nodeMatches = nodeMatcher.resolveCompoundExpr(compoundExpr);

                return plan(nodeMatches);
            }
        });
    }



    private ColumnPlanNode plan(Collection<NodeMatch> matches) {
        if(matches.size() == 1) {
            return plan(matches.iterator().next());
        } else {
            List<ColumnPlanNode> nodes = new ArrayList<>();
            for (NodeMatch match : matches) {
                nodes.add(plan(match));
            }
            return new VectorOp(CoalesceFunction.INSTANCE, nodes);
        }
    }

    private ColumnPlanNode plan(NodeMatch match) {

        if(match.isJoined()) {
            throw new UnsupportedOperationException("TODO: Joins");
        }

        switch (match.getType()) {
            case ID:
                return new IdFetchOp(match.getFormClass().getId());
            case CLASS:
                throw new UnsupportedOperationException("TODO: class");
            default:
                throw new UnsupportedOperationException("type: " + match.getType());
            case FIELD:
                return new ColumnFetchOp(match.getFieldNode().getDefiningFormClass().getId(),
                        match.getFieldNode().getField());
        }
    }
}
