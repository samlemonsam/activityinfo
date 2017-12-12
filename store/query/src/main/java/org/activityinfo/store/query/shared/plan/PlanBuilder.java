package org.activityinfo.store.query.shared.plan;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.activityinfo.model.expr.*;
import org.activityinfo.model.expr.diagnostic.ExprException;
import org.activityinfo.model.expr.functions.CoalesceFunction;
import org.activityinfo.model.expr.functions.IfFunction;
import org.activityinfo.model.expr.functions.StatFunction;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.query.shared.NodeMatcher;

import java.util.*;
import java.util.logging.Logger;

/**
 * Constructs a {@code PlanNode} DAG from a formula, resolving any
 * symbolic references and expanding calculated fields.
 */
public class PlanBuilder implements ExprVisitor<PlanNode> {

    private static final Logger LOGGER = Logger.getLogger(PlanBuilder.class.getName());

    private final FormTree formTree;
    private final NodeMatcher resolver;

    private final Deque<SymbolExpr> evaluationStack = new ArrayDeque<>();

    public PlanBuilder(FormTree formTree) {
        this.formTree = formTree;
        this.resolver = new NodeMatcher(formTree);
    }

    @Override
    public PlanNode visitConstant(ConstantExpr node) {
        return new ConstantPlanNode(node);
    }

    @Override
    public PlanNode visitGroup(GroupExpr expr) {
        return expr.accept(this);
    }

    @Override
    public PlanNode visitSymbol(SymbolExpr symbolExpr) {

        // Check for recursion: are we in the process of evaluating
        // this symbol? Trying to do so again will lead to an infinite
        // loop and a StackOverflowException.

        if(evaluationStack.contains(symbolExpr)) {
            return new ErrorNode(symbolExpr, "Recursive expression");
        }

        evaluationStack.push(symbolExpr);

        try {

            if (symbolExpr.getName().equals(ColumnModel.ID_SYMBOL)) {
                return new RecordIdNode(formTree.getRootFormClass().getId());

            } else if (symbolExpr.getName().equals(ColumnModel.CLASS_SYMBOL)) {
                return new ConstantPlanNode(formTree.getRootFormClass().getId().asString());
            }

            Collection<PlanNode> nodes = resolver.resolveSymbol(symbolExpr);
            LOGGER.finer(symbolExpr + " matched to " + nodes);
            return combineNodes(Lists.newArrayList(nodes));

        } finally {
            evaluationStack.pop();
        }
    }

    @Override
    public PlanNode visitCompoundExpr(CompoundExpr compoundExpr) {
        return combineNodes(Lists.newArrayList(resolver.resolveCompoundExpr(compoundExpr)));
    }

    @Override
    public PlanNode visitFunctionCall(final FunctionCallNode call) {
        if(call.getArguments().isEmpty()) {
            return createNullaryFunctionCall(call);
        } else {
            return createFunctionNode(call);
        }
    }

    private PlanNode createNullaryFunctionCall(final FunctionCallNode call) {
        // We expect all our formula functions to be "pure" : that is have no side effects, and
        // to consistently return the same value given the same arguments.

        // For this reason, a function which takes no arguments must be constant.

        // Technically, the TODAY() function violates this rule, but it will be still be constant
        // for the duration of the query.

        FieldValue constantValue = call.getFunction().apply(Collections.<FieldValue>emptyList());
        FieldType constantType = call.getFunction().resolveResultType(Collections.<FieldType>emptyList());

        return new ConstantPlanNode(new ConstantExpr(constantValue, constantType));
    }

    private PlanNode createFunctionNode(final FunctionCallNode call) {

        List<PlanNode> argumentNodes = Lists.newArrayList();
        List<FieldType> argumentTypes = Lists.newArrayList();

        for (ExprNode argument : call.getArguments()) {
            PlanNode argumentNode = argument.accept(this);
            if(call.getFunction() instanceof StatFunction) {
                argumentNode = new AggregateNode((StatFunction) call.getFunction(), argumentNode);
            }
            argumentNodes.add(argumentNode);
            argumentTypes.add(argumentNode.getFieldType());
        }

        FieldType resultType = call.getFunction().resolveResultType(argumentTypes);

        return new FunctionPlanNode(call.getFunction(), argumentNodes, resultType);
    }

    private PlanNode combineNodes(List<PlanNode> expandedNodes) {
        if(expandedNodes.isEmpty()) {
            return new ConstantPlanNode((String)null);

        } else if(expandedNodes.size() == 1) {
            return expandedNodes.get(0);
        } else {
            return new FunctionPlanNode(CoalesceFunction.INSTANCE, expandedNodes,
                    expandedNodes.get(0).getFieldType());
        }
    }

    private PlanNode expandCalculatedField(FieldPlanNode node) {
        ExprNode exprNode;
        try {
            exprNode = ExprParser.parse(node.getCalculation());
        } catch (ExprException e) {
            return new ErrorNode(node.getExpr(), e.getMessage());
        }

        PlanNode calculatedNode = exprNode.accept(this);
        FieldType calculatedType = calculatedNode.getFieldType();

        ExprNode relevanceFormula = tryParseRelevance(node);
        if(relevanceFormula == null) {
            return calculatedNode;

        } else {
            PlanNode conditionNode = relevanceFormula.accept(this);
            PlanNode nullNode = new ConstantPlanNode(new ConstantExpr(null, calculatedType));

            // Null out the calculated values if the field is not relevant for a record
            return new FunctionPlanNode(IfFunction.INSTANCE,
                    Arrays.asList(conditionNode, calculatedNode, nullNode),
                    calculatedType);
        }
    }

    private ExprNode tryParseRelevance(FieldPlanNode node) {
        String formula = node.getFieldNode().getField().getRelevanceConditionExpression();
        if(Strings.isNullOrEmpty(formula)) {
            return null;
        }
        try {
            return ExprParser.parse(formula);
        } catch (ExprException e) {
            LOGGER.info("Failed to parse relevance condition " + formula);
            return null;
        }
    }
}
