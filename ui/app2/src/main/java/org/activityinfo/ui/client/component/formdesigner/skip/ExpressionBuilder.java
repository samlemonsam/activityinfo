package org.activityinfo.ui.client.component.formdesigner.skip;
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
 *
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.FunctionCallNode;
import org.activityinfo.model.expr.GroupExpr;
import org.activityinfo.model.expr.functions.ExprFunction;

import java.util.List;

/**
 * @author yuriyz on 7/25/14.
 */
public class ExpressionBuilder {

    private List<RowData> rows;

    public ExpressionBuilder(List<RowData> rows) {
        this.rows = rows;
    }

    public String build() {

        if(rows.isEmpty()) {
            return null;
        }

        ExprNode left = rows.get(0).buildPredicateExpr();

        // If there is more than one conditions, then combine them
        // using "AND" and "OR"
        for (int i = 1; i < rows.size(); i++) {
            ExprNode right = maybeGroup(rows.get(i).buildPredicateExpr());
            ExprFunction op = rows.get(i).getJoinFunction();
            left = new FunctionCallNode(op, maybeGroup(left), right);
        }

        return left.asExpression();
    }

    private ExprNode maybeGroup(ExprNode exprNode) {
        if(exprNode instanceof GroupExpr) {
            return exprNode;
        } else {
            return new GroupExpr(exprNode);
        }
    }


}
