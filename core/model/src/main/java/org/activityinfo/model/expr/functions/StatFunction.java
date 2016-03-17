package org.activityinfo.model.expr.functions;

import com.google.common.collect.Sets;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;

import java.util.List;
import java.util.Set;

/**
 * Base class for functions which return a statistical summary of values
 */
public abstract class StatFunction extends ExprFunction {


    @Override
    public final FieldType resolveResultType(List<FieldType> argumentTypes) {
        return new QuantityType(computeUnits(argumentTypes));
    }
    
    protected final String computeUnits(List<FieldType> argumentTypes) {
        Set<String> units = Sets.newHashSet();
        for (FieldType argumentType : argumentTypes) {
            if(argumentType instanceof QuantityType) {
                QuantityType quantityType = (QuantityType) argumentType;
                units.add(quantityType.getUnits());
            }
        }
        if(units.size() == 1) {
            return units.iterator().next();
        } else {
            return Quantity.UNKNOWN_UNITS;
        }
    }
}
