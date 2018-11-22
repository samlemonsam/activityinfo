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
package org.activityinfo.model.formula.eval;

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.TypedFormRecord;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.FormulaParser;
import org.activityinfo.model.formula.diagnostic.CircularReferenceException;
import org.activityinfo.model.formula.diagnostic.FormulaException;
import org.activityinfo.model.type.ErrorValue;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.MissingFieldType;
import org.activityinfo.model.type.expr.CalculatedFieldType;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CalculatedField implements FieldValueSource {

    private static final Logger LOGGER = Logger.getLogger(CalculatedField.class.getName());

    private final FormField field;
    private FormulaNode expr;
    private ErrorValue errorValue;


    /**
     * True if this expression is being evaluated. Used to trap circular
     * references.
     */
    private boolean evaluating = false;

    public CalculatedField(FormField field) {
        this.field = field;
        CalculatedFieldType type = (CalculatedFieldType) field.getType();
        try {
            expr = FormulaParser.parse(type.getExpression());
        } catch(FormulaException e) {
            LOGGER.log(Level.WARNING, "Expression failed to parse: " + type.getExpression(), e);
            expr = null;
            errorValue = new ErrorValue(e);
        }
    }

    @Override
    public FieldValue getValue(TypedFormRecord instance, EvalContext context) {
        if(errorValue != null) {
            return errorValue;
        }
        if(evaluating) {
            throw new CircularReferenceException(field.getCode());
        }
        evaluating = true;
        try {
            return expr.evaluate(context);
        } finally {
            evaluating = false;
        }
    }

    @Override
    public FieldType resolveType(EvalContext context) {
        if(errorValue != null) {
            return MissingFieldType.INSTANCE;
        }
        try {
            return expr.resolveType(context);

        } catch(Exception e) {
            return MissingFieldType.INSTANCE;
        }
    }

    @Override
    public FormField getField() {
        return field;
    }
}
