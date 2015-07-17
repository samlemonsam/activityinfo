package org.activityinfo.model.expr.eval;

import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.ExprParser;
import org.activityinfo.model.expr.diagnostic.CircularReferenceException;
import org.activityinfo.model.expr.diagnostic.ExprException;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.Resource;
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
    private ExprNode expr;
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
            expr = ExprParser.parse(type.getExpression().getExpression());
        } catch(ExprException e) {
            LOGGER.log(Level.WARNING, "Expression failed to parse: " + type.getExpression(), e);
            expr = null;
            errorValue = new ErrorValue(e);
        }
    }

    @Override
    public FieldValue getValue(Resource instance, EvalContext context) {
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
