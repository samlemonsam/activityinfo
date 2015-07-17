package org.activityinfo.model.expr.functions;

import com.google.common.base.Preconditions;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.BooleanType;

import java.util.List;

public class NotFunction extends ExprFunction {

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
        Preconditions.checkArgument(arguments.size() == 1);
        boolean x = Casting.toBoolean(arguments.get(0));
        return BooleanFieldValue.valueOf(!x);
    }

    @Override
    public FieldType resolveResultType(List<FieldType> argumentTypes) {
        return BooleanType.INSTANCE;
    }
}
