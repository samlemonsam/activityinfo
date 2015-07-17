package org.activityinfo.model.expr.functions;

import com.google.common.base.Preconditions;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.BooleanType;

import java.util.List;

public abstract class BinaryBooleanOperator extends ExprFunction {

    private final String name;

    protected BinaryBooleanOperator(String name) {
        this.name = name;
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public String getLabel() {
        return name;
    }

    @Override
    public BooleanFieldValue apply(List<FieldValue> arguments) {
        Preconditions.checkArgument(arguments.size() == 2);
        boolean a = Casting.toBoolean(arguments.get(0));
        boolean b = Casting.toBoolean(arguments.get(1));

        return BooleanFieldValue.valueOf(apply(a, b));

    }

    @Override
    public FieldType resolveResultType(List<FieldType> argumentTypes) {
        return BooleanType.INSTANCE;
    }

    public abstract boolean apply(boolean a, boolean b);
}
