package org.activityinfo.model.expr.functions;

import org.activityinfo.model.expr.diagnostic.ArgumentException;
import org.activityinfo.model.expr.diagnostic.ExprSyntaxException;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.BooleanType;

import java.util.List;


public class IfFunction extends ExprFunction {


    public static final IfFunction INSTANCE = new IfFunction();

    private IfFunction() {
    }

    @Override
    public String getId() {
        return "if";
    }

    @Override
    public String getLabel() {
        return "IF";
    }

    @Override
    public FieldValue apply(List<FieldValue> arguments) {
        checkArity(arguments, 3);

        BooleanFieldValue condition = toBoolean(arguments.get(0));
        FieldValue ifTrue = arguments.get(1);
        FieldValue ifFalse = arguments.get(2);

        if(condition == BooleanFieldValue.TRUE) {
            return ifTrue;
        } else {
            return ifFalse;
        }
    }

    private BooleanFieldValue toBoolean(FieldValue fieldValue) {
        if(fieldValue instanceof BooleanFieldValue) {
            return (BooleanFieldValue) fieldValue;
        } else {
            throw new ExprSyntaxException("IF() condition must boolean.");
        }
    }

    @Override
    public FieldType resolveResultType(List<FieldType> argumentTypes) {
        checkArity(argumentTypes, 3);

        FieldType conditionType = argumentTypes.get(0);
        if(!(conditionType instanceof BooleanType)) {
            throw new ArgumentException(0, "Expected TRUE/FALSE value");
        }

        FieldType trueType = argumentTypes.get(1);
        FieldType falseType = argumentTypes.get(2);
        if(trueType.getTypeClass() != falseType.getTypeClass()) {
            throw new ArgumentException(2, "Must have the same type as the TRUE argument.");
        }
        return trueType;
    }

}
