package org.activityinfo.model.expr.functions;

public class BooleanFunctions {

    public static final ExprFunction AND = AndFunction.INSTANCE;
    public static final ExprFunction OR = OrFunction.INSTANCE;
    public static final ExprFunction EQUAL = EqualFunction.INSTANCE;
    public static final ExprFunction NOT_EQUAL = NotEqualFunction.INSTANCE;

    public static final ExprFunction GREATER = GreaterFunction.INSTANCE;
    public static final ExprFunction GREATER_OR_EQUAL = GreaterOrEqualFunction.INSTANCE;
    public static final ExprFunction LESS = LessFunction.INSTANCE;
    public static final ExprFunction LESS_OR_EQUAL = LessOrEqualFunction.INSTANCE;

}
