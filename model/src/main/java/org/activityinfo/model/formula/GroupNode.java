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

import com.google.common.base.Function;
import org.activityinfo.model.formula.eval.EvalContext;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;

/**
 * An expression group ()
 */
public class GroupNode extends FormulaNode {

    private FormulaNode expr;

    public GroupNode(FormulaNode expr) {
        super();
        this.expr = expr;
    }

    public GroupNode(FormulaNode expr, SourceRange sourceRange) {
        super();
        this.expr = expr;
        this.sourceRange = sourceRange;
    }

    @Override
    public String toString() {
        return asExpression();
    }

    public String asExpression() {
        return "(" + expr.asExpression() + ")";
    }

    @Override
    public <T> T accept(FormulaVisitor<T> visitor) {
        return visitor.visitGroup(this);
    }

    @Override
    public FormulaNode transform(Function<FormulaNode, FormulaNode> function) {
        return function.apply(new GroupNode(expr.transform(function)));
    }

    @Override
    public FieldValue evaluate(EvalContext context) {
        return expr.evaluate(context);
    }

    @Override
    public FieldType resolveType(EvalContext context) {
        return expr.resolveType(context);
    }

    public FormulaNode getExpr() {
        return expr;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((expr == null) ? 0 : expr.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        GroupNode other = (GroupNode) obj;
        return other.expr.equals(expr);
    }
}
