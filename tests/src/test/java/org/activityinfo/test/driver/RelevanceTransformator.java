package org.activityinfo.test.driver;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.activityinfo.model.expr.*;

import java.util.List;

/**
 * @author yuriyz on 12/18/2015.
 */
public class RelevanceTransformator {

    private final AliasTable aliases;

    public RelevanceTransformator(AliasTable aliases) {
        this.aliases = aliases;
    }

    public String transform(String relevanceExpression) {
        ExprLexer lexer = new ExprLexer(relevanceExpression);
        ExprParser parser = new ExprParser(lexer);
        ExprNode expr = parser.parse();
        return expr.accept(new TransformationVisitor(aliases)).asExpression();
    }

    private static class TransformationVisitor implements ExprVisitor<ExprNode> {

        private final AliasTable aliases;

        public TransformationVisitor(AliasTable aliases) {
            this.aliases = aliases;
        }

        @Override
        public ExprNode visitConstant(ConstantExpr node) {
            return node;
        }

        @Override
        public ExprNode visitSymbol(SymbolExpr symbolExpr) {
            symbolExpr.setName(aliases.getAlias(symbolExpr.getName()));
            return symbolExpr;
        }

        @Override
        public ExprNode visitGroup(GroupExpr expr) {
            return expr;
        }

        @Override
        public ExprNode visitCompoundExpr(CompoundExpr compoundExpr) {
            return compoundExpr;
        }

        @Override
        public ExprNode visitFunctionCall(FunctionCallNode functionCallNode) {
            List<ExprNode> arguments = functionCallNode.getArguments();
            for (ExprNode node : arguments) {
                if (node instanceof SymbolExpr) {
                    visitSymbol((SymbolExpr) node);
                }
            }
            return functionCallNode;
        }
    }
}
