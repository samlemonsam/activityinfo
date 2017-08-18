package org.activityinfo.model.expr.functions;

import org.activityinfo.model.expr.diagnostic.ExprSyntaxException;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;

import java.util.List;

public abstract class UnaryFunctionBase extends ExprFunction implements ColumnFunction {

    private final String name;

    private static int MAXARGS = 1;

    protected UnaryFunctionBase(String name) { this.name = name; }

    @Override
    public String getId() { return name; }

    @Override
    public String getLabel() { return name; }

    @Override
    public FieldValue apply(List<FieldValue> arguments) {
        checkArity(arguments,MAXARGS);
        FieldValue unaryArg = arguments.get(0);
        return apply(unaryArg);
    }

    @Override
    public ColumnView columnApply(int numRows, List<ColumnView> arguments) {
        checkArity(arguments,MAXARGS);
        ColumnView unaryArgument = arguments.get(0);
        return columnApply(numRows,unaryArgument);
    }

    @Override
    public FieldType resolveResultType(List<FieldType> argumentTypes) {
        if(argumentTypes.size() != 1) {
            throw new ExprSyntaxException("Expected single argument");
        }
        FieldType unaryArgType = argumentTypes.get(0);
        return resolveUnaryResultType(unaryArgType);
    }

    public abstract FieldType resolveUnaryResultType(FieldType argumentType);

    /**
     * Apply the function to a single {@code argument}
     */
    public abstract FieldValue apply(FieldValue argument);

    public abstract ColumnView columnApply(int numRows, ColumnView argument);

}
