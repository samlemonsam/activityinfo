package org.activityinfo.model.expr.functions;

public class BooleanFunctions {

    public static final ExprFunction AND = AndFunction.INSTANCE;
    public static final ExprFunction OR = OrFunction.INSTANCE;
    public static final ComparisonOperator EQUAL = EqualFunction.INSTANCE;
    public static final ComparisonOperator NOT_EQUAL = NotEqualFunction.INSTANCE;

    public static final ComparisonOperator GREATER = GreaterFunction.INSTANCE;
    public static final ComparisonOperator GREATER_OR_EQUAL = GreaterOrEqualFunction.INSTANCE;
    public static final ComparisonOperator LESS = LessFunction.INSTANCE;
    public static final ComparisonOperator LESS_OR_EQUAL = LessOrEqualFunction.INSTANCE;

}
