package org.activityinfo.model.expr;

import com.google.common.collect.Lists;
import org.activityinfo.model.expr.functions.*;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.number.Quantity;

import java.util.*;

/**
 * Functions for programatically building expressions
 */
public class Exprs {
    
    public static SymbolExpr symbol(String name) {
        return new SymbolExpr(name);
    }
    
    public static SymbolExpr symbol(ResourceId resourceId) {
        return new SymbolExpr(resourceId);
    }
    
    public static FunctionCallNode equals(ExprNode x, ExprNode y) {
        return new FunctionCallNode(EqualFunction.INSTANCE, x, y);
    }
    
    public static FunctionCallNode equals(ResourceId x, ResourceId y) {
        return new FunctionCallNode(EqualFunction.INSTANCE, symbol(x), symbol(y));
    }
    
    public static ExprNode allTrue(Iterable<ExprNode> nodes) {
        return binaryTree(AndFunction.INSTANCE, nodes);
    }
    
    public static ExprNode anyTrue(List<ExprNode> nodes) {
        if(nodes.isEmpty()) {
            return new ConstantExpr(false);
        } else {
            return binaryTree(OrFunction.INSTANCE, nodes);
        }
    }
    
    private static ExprNode binaryTree(ExprFunction function, Iterable<ExprNode> nodes) {
        Iterator<ExprNode> it = nodes.iterator();
        ExprNode expr = it.next();
        while(it.hasNext()) {
            expr = new FunctionCallNode(function, expr, it.next());
        }
        return expr;
    }

    public static ExprNode idConstant(ResourceId id) {
        return new ConstantExpr(id.asString());
    }
    
    public static ExprNode idEqualTo(ResourceId id) {
        return equals(symbol(ColumnModel.ID_SYMBOL), idConstant(id));
    }
    
    public static ExprNode idEqualTo(Set<ResourceId> ids) {
        List<ExprNode> conditions = new ArrayList<>();
        for (ResourceId id : ids) {
            conditions.add(idEqualTo(id));
        }
        return anyTrue(conditions);
    }

    public static FunctionCallNode call(ExprFunction function, ExprNode... argumentList) {
        return new FunctionCallNode(function, argumentList);
    }

    /**
     * Tries to decompose a tree of binary operations into a list of operands. (A && B && C) or
     * (A || B || C) or (A + B + C) => [A, B, C]
     */
    public static List<ExprNode> findBinaryTree(ExprNode rootNode, ExprFunction operator) {
        List<ExprNode> list = new ArrayList<>();
        findBinaryTree(rootNode, list, operator);

        return list;
    }

    private static void findBinaryTree(ExprNode node, List<ExprNode> list, ExprFunction operator) {

        // Unwrap group expressions ((A))
        node = simplify(node);

        if(isBinaryOperation(node, operator)) {
            // If this expression is in the form A && B, then descend
            // recursively
            FunctionCallNode callNode = (FunctionCallNode) node;
            findBinaryTree(callNode.getArgument(0), list, operator);
            findBinaryTree(callNode.getArgument(1), list, operator);

        } else {
            // If not a conjunction, then add this node to the list
            list.add(node);
        }
    }

    public static ExprNode simplify(ExprNode node) {
        while(node instanceof GroupExpr) {
            node = ((GroupExpr) node).getExpr();
        }
        return node;
    }

    private static boolean isBinaryOperation(ExprNode node, ExprFunction operator) {
        if(node instanceof FunctionCallNode) {
            FunctionCallNode callNode = (FunctionCallNode) node;
            return callNode.getArgumentCount() == 2 &&
                   callNode.getFunction() == operator;
        }
        return false;
    }

    public static ExprNode coalesce(Collection<ExprNode> nodes) {
        if(nodes.isEmpty()) {
            return new ConstantExpr((Quantity)null);
        } else if (nodes.size() == 1) {
            return nodes.iterator().next();
        } else {
            return new FunctionCallNode(CoalesceFunction.INSTANCE, Lists.newArrayList(nodes));
        }
    }
}
