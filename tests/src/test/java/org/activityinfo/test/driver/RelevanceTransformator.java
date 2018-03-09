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

import org.activityinfo.model.formula.*;

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
        FormulaLexer lexer = new FormulaLexer(relevanceExpression);
        FormulaParser parser = new FormulaParser(lexer);
        FormulaNode expr = parser.parse();
        return expr.accept(new TransformationVisitor(aliases)).asExpression();
    }

    private static class TransformationVisitor implements FormulaVisitor<FormulaNode> {

        private final AliasTable aliases;

        public TransformationVisitor(AliasTable aliases) {
            this.aliases = aliases;
        }

        @Override
        public FormulaNode visitConstant(ConstantNode node) {
            return node;
        }

        @Override
        public FormulaNode visitSymbol(SymbolNode symbolNode) {
            symbolNode.setName(aliases.getAlias(symbolNode.getName()));
            return symbolNode;
        }

        @Override
        public FormulaNode visitGroup(GroupNode expr) {
            return expr;
        }

        @Override
        public FormulaNode visitCompoundExpr(CompoundExpr compoundExpr) {
            return compoundExpr;
        }

        @Override
        public FormulaNode visitFunctionCall(FunctionCallNode functionCallNode) {
            List<FormulaNode> arguments = functionCallNode.getArguments();
            for (FormulaNode node : arguments) {
                if (node instanceof SymbolNode) {
                    visitSymbol((SymbolNode) node);
                }
            }
            return functionCallNode;
        }
    }
}
