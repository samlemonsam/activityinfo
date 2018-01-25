package org.activityinfo.model.expr.functions;

import org.activityinfo.model.expr.diagnostic.ArgumentException;
import org.activityinfo.model.expr.diagnostic.ExprSyntaxException;
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
        checkArity(arguments,2);
        boolean a = Casting.toBoolean(arguments.get(0));
        boolean b = Casting.toBoolean(arguments.get(1));

        return BooleanFieldValue.valueOf(apply(a, b));
    }

    @Override
    public final boolean isInfix() {
        return true;
    }

    @Override
    public ColumnView columnApply(int numRows, List<ColumnView> arguments) {
        checkArity(arguments,2);

        ColumnView a = arguments.get(0);
        ColumnView b = arguments.get(1);

        if(a.numRows() != b.numRows()) {
            throw new ExprSyntaxException("Arguments must have the same number of rows");
        }

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
        if(argumentTypes.size() != 2) {
            throw new ExprSyntaxException("Expected two arguments");
        }
        if(!(argumentTypes.get(0) instanceof BooleanType)) {
            throw new ArgumentException(0, "Expected TRUE/FALSE value");
        }
        if(!(argumentTypes.get(1) instanceof BooleanType)) {
            throw new ArgumentException(1, "Expected TRUE/FALSE value");
        }
        return BooleanType.INSTANCE;
    }

    public abstract boolean apply(boolean a, boolean b);

    /**
     * Apply the function to {@code a} and {@code b}, which must have the values 
     * {@link ColumnView#TRUE}, {@link ColumnView#FALSE} or {@link ColumnView#NA}
     */
    public abstract int apply(int a, int b);

}
