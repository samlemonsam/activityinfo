package org.activityinfo.model.formula.functions;

import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.BooleanType;

import java.util.List;

public class NotFunction extends FormulaFunction {

    public static final NotFunction INSTANCE = new NotFunction();

    private NotFunction() {}

    @Override
    public String getId() {
        return "!";
    }

    @Override
    public String getLabel() {
        return getId();
    }

    @Override
    public BooleanFieldValue apply(List<FieldValue> arguments) {
        checkArity(arguments,1);
        boolean x = Casting.toBoolean(arguments.get(0));
        return BooleanFieldValue.valueOf(!x);
    }

    @Override
    public FieldType resolveResultType(List<FieldType> argumentTypes) {
        return BooleanType.INSTANCE;
    }
}
