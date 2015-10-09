package org.activityinfo.model.lock;
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

import org.activityinfo.model.expr.ExprLexer;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.ExprParser;
import org.activityinfo.model.expr.functions.Casting;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormEvalContext;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.type.FieldValue;

/**
 * @author yuriyz on 10/09/2015.
 */
public class LockEvaluator {

    private final FormClass formClass;

    public LockEvaluator(FormClass formClass) {
        this.formClass = formClass;
    }

    public boolean isLocked(FormInstance instance) {
        if (formClass.getLocks().isEmpty()) {
            return false;
        }

        for (ResourceLock lock : formClass.getLocks()) {
            if (isLocked(lock, instance)) {
                return true;
            }
        }
        return false;
    }

    public boolean isLockedSilently(FormInstance instance) {
        try {
            return isLocked(instance);
        } catch (UnsupportedOperationException e) {
            // there are numerous use cases when operand may be null, which is not supported by comparison operators by design
            // example: user selected start date (end date is not selected yet)
            return false;
        }
    }

    private boolean isLocked(ResourceLock lock, FormInstance instance) {
        if (!lock.getEnabled()) {
            return false;
        }

        ExprLexer lexer = new ExprLexer(lock.getExpression());
        ExprParser parser = new ExprParser(lexer);
        ExprNode expr = parser.parse();
        FieldValue result = expr.evaluate(new FormEvalContext(formClass, instance));
        return Casting.toBoolean(result);
    }
}
