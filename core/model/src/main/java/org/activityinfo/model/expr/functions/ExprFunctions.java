package org.activityinfo.model.expr.functions;

import java.util.HashMap;
import java.util.Map;

public final class ExprFunctions {

    private static Map<String, ExprFunction> lookupMap;

    private ExprFunctions() {
    }

    private static void register(ExprFunction function) {
        lookupMap.put(function.getId(), function);
    }

    public static ExprFunction get(String name) {
        if (lookupMap == null) {
            lookupMap = new HashMap<>();
            register(AndFunction.INSTANCE);
            register(DivideFunction.INSTANCE);
            register(EqualFunction.INSTANCE);
            register(new MinusFunction());
            register(new MultiplyFunction());
            register(NotEqualFunction.INSTANCE);
            register(NotFunction.INSTANCE);
            register(OrFunction.INSTANCE);
            register(PlusFunction.INSTANCE);
            register(ContainsAllFunction.INSTANCE);
            register(ContainsAnyFunction.INSTANCE);
            register(NotContainsAllFunction.INSTANCE);
            register(NotContainsAnyFunction.INSTANCE);
        }

        ExprFunction exprFunction = lookupMap.get(name);
        if (exprFunction == null) {
            throw new UnsupportedOperationException("No such function '" + name + "'");
        }
        return exprFunction;
    }
}
