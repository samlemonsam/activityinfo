package org.activityinfo.model.formula.functions;

import org.activityinfo.model.formula.functions.date.*;

import java.util.HashMap;
import java.util.Map;

public final class FormulaFunctions {


    /**
     * Avoids race conditions by using the <a href="https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom">
     *     Initialization-on-demand holder idiom</a>
     */
    private static class MapHolder {
        private static final MapHolder INSTANCE = new MapHolder();
        
        private Map<String, FormulaFunction> lookupMap = new HashMap<>();

        private void register(FormulaFunction function) {
            lookupMap.put(function.getId().toLowerCase(), function);
        }
        
        public MapHolder() {
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
            register(BooleanFunctions.GREATER);
            register(BooleanFunctions.GREATER_OR_EQUAL);
            register(BooleanFunctions.LESS);
            register(BooleanFunctions.LESS_OR_EQUAL);

            register(SumFunction.INSTANCE);
            register(AverageFunction.INSTANCE);
            register(MedianFunction.INSTANCE);
            register(MaxFunction.INSTANCE);
            register(MinFunction.INSTANCE);
            register(CountFunction.INSTANCE);

            register(BoundingBoxFunction.XMIN);
            register(BoundingBoxFunction.YMIN);
            register(BoundingBoxFunction.XMAX);
            register(BoundingBoxFunction.YMAX);
            register(IfFunction.INSTANCE);

            register(DateFunction.INSTANCE);
            register(YearFunction.INSTANCE);
            register(MonthFunction.INSTANCE);
            register(QuarterFunction.INSTANCE);
            register(DayFunction.INSTANCE);
            register(YearFracFunction.INSTANCE);
            register(TodayFunction.INSTANCE);

            register(CeilingFunction.INSTANCE);
            register(FloorFunction.INSTANCE);

            register(SearchFunction.INSTANCE);
            register(IsNumberFunction.INSTANCE);

            register(AddDateFunction.INSTANCE);

            register(CoalesceFunction.INSTANCE);
        }
        
        public FormulaFunction get(String name) {

            FormulaFunction formulaFunction = lookupMap.get(name.toLowerCase());
            if (formulaFunction == null) {
                throw new UnsupportedOperationException("No such function '" + name + "'");
            }
            return formulaFunction;
        }
    }
    
    private FormulaFunctions() {
    }

    public static FormulaFunction get(String name) {
        return MapHolder.INSTANCE.get(name);
    }
}
