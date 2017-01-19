package org.activityinfo.model.expr.functions;

import com.google.common.base.Preconditions;
import org.activityinfo.model.query.BooleanColumnView;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.BooleanType;

import java.util.List;

public abstract class BinaryBooleanOperator extends ExprFunction implements ColumnFunction {

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
    public final boolean isInfix() {
        return true;
    }

    @Override
    public ColumnView columnApply(List<ColumnView> arguments) {
        Preconditions.checkArgument(arguments.size() == 2);

        ColumnView a = arguments.get(0);
        ColumnView b = arguments.get(1);

        Preconditions.checkArgument(a.numRows() == b.numRows(), "arguments must have the same number of rows");

        int[] result = new int[a.numRows()];
        for (int i = 0; i < result.length; i++) {
            int ai = a.getBoolean(i);
            int bi = b.getBoolean(i);
            result[i] = apply(ai, bi);
        }
        return new BooleanColumnView(result);
    }

    @Override
    public FieldType resolveResultType(List<FieldType> argumentTypes) {
        return BooleanType.INSTANCE;
    }

    public abstract boolean apply(boolean a, boolean b);

    /**
     * Apply the function to {@code a} and {@code b}, which must have the values 
     * {@link ColumnView#TRUE}, {@link ColumnView#FALSE} or {@link ColumnView#NA}
     */
    public abstract int apply(int a, int b);

}
