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
import org.activityinfo.model.formula.functions.Casting;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;


/**
 * Root of the formula Abstract Syntax Tree (AST). Formulas are used for validation and
 * calculation.
 */
public abstract class FormulaNode {

    SourceRange sourceRange;

    /**
     * Evaluates the expression to a real value.
     * @param context
     */
    public abstract FieldValue evaluate(EvalContext context);

    /**
     *
     * @return the FieldType of the expression node
     */
    public abstract FieldType resolveType(EvalContext context);

    public abstract String asExpression();

    public boolean evaluateAsBoolean(EvalContext context) {
        FieldValue fieldValue = evaluate(context);
        return Casting.toBoolean(fieldValue);
    }

    public abstract <T> T accept(FormulaVisitor<T> visitor);

    public SourceRange getSourceRange() {
        return sourceRange;
    }

    public FormulaNode transform(Function<FormulaNode, FormulaNode> function) {
        return function.apply(this);
    }
}
