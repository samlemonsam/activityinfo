/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.model.formula;

import org.activityinfo.model.formula.functions.AndFunction;
import org.activityinfo.model.formula.functions.EqualFunction;
import org.activityinfo.model.formula.functions.FormulaFunction;
import org.activityinfo.model.formula.functions.OrFunction;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.resource.ResourceId;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Functions for manipulating formulas
 */
public class Formulas {
    
    public static SymbolNode symbol(String name) {
        return new SymbolNode(name);
    }
    
    public static SymbolNode symbol(ResourceId resourceId) {
        return new SymbolNode(resourceId);
    }
    
    public static FunctionCallNode equals(FormulaNode x, FormulaNode y) {
        return new FunctionCallNode(EqualFunction.INSTANCE, x, y);
    }
    
    public static FunctionCallNode equals(ResourceId x, ResourceId y) {
        return new FunctionCallNode(EqualFunction.INSTANCE, symbol(x), symbol(y));
    }
    
    public static FormulaNode allTrue(Iterable<FormulaNode> nodes) {
        return binaryTree(AndFunction.INSTANCE, nodes);
    }
    
    public static FormulaNode anyTrue(List<FormulaNode> nodes) {
        if(nodes.isEmpty()) {
            return new ConstantNode(false);
        } else {
            return binaryTree(OrFunction.INSTANCE, nodes);
        }
    }
    
    private static FormulaNode binaryTree(FormulaFunction function, Iterable<FormulaNode> nodes) {
        Iterator<FormulaNode> it = nodes.iterator();
        FormulaNode expr = it.next();
        while(it.hasNext()) {
            expr = new FunctionCallNode(function, expr, it.next());
        }
        return expr;
    }

    public static FormulaNode idConstant(ResourceId id) {
        return new ConstantNode(id.asString());
    }
    
    public static FormulaNode idEqualTo(ResourceId id) {
        return equals(symbol(ColumnModel.ID_SYMBOL), idConstant(id));
    }
    
    public static FormulaNode idEqualTo(Set<ResourceId> ids) {
        List<FormulaNode> conditions = new ArrayList<>();
        for (ResourceId id : ids) {
            conditions.add(idEqualTo(id));
        }
        return anyTrue(conditions);
    }

    public static FunctionCallNode call(FormulaFunction function, FormulaNode... argumentList) {
        return new FunctionCallNode(function, argumentList);
    }

    /**
     * Tries to decompose a tree of binary operations into a list of operands. (A && B && C) or
     * (A || B || C) or (A + B + C) => [A, B, C]
     */
    public static List<FormulaNode> findBinaryTree(FormulaNode rootNode, FormulaFunction operator) {
        List<FormulaNode> list = new ArrayList<>();
        findBinaryTree(rootNode, list, operator);

        return list;
    }

    private static void findBinaryTree(FormulaNode node, List<FormulaNode> list, FormulaFunction operator) {

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

    public static FormulaNode simplify(FormulaNode node) {
        while(node instanceof GroupNode) {
            node = ((GroupNode) node).getExpr();
        }
        return node;
    }

    private static boolean isBinaryOperation(FormulaNode node, FormulaFunction operator) {
        if(node instanceof FunctionCallNode) {
            FunctionCallNode callNode = (FunctionCallNode) node;
            return callNode.getArgumentCount() == 2 &&
                   callNode.getFunction() == operator;
        }
        return false;
    }

}
