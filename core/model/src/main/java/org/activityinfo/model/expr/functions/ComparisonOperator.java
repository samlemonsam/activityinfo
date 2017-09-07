package org.activityinfo.model.expr.functions;

import org.activityinfo.model.expr.diagnostic.ExprSyntaxException;
import org.activityinfo.model.query.BooleanColumnView;
import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.EnumColumnView;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.model.type.time.LocalDate;

import java.util.List;

public abstract class ComparisonOperator extends ExprFunction implements ColumnFunction {

    private final String name;

    public ComparisonOperator(String name) {
        this.name = name;
    }

    @Override
    public final String getId() {
        return name;
    }

    @Override
    public String getLabel() {
        return name;
    }

    @Override
    public FieldValue apply(List<FieldValue> arguments) {
        FieldValue a = arguments.get(0);
        FieldValue b = arguments.get(1);

        // special handling if right operand is constant without units specified: {fieldId}==2.0
        // then construct copy of Quantity without unit information for correct comparison.
        if (a instanceof Quantity && b instanceof Quantity) {
            Quantity aQuantity = (Quantity) a;
            Quantity bQuantity = (Quantity) b;
            if (Quantity.UNKNOWN_UNITS.equals(aQuantity.getUnits()) || Quantity.UNKNOWN_UNITS.equals(bQuantity.getUnits())) {
                a = new Quantity(aQuantity.getValue());
                b = new Quantity(bQuantity.getValue());
            }
        }

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
        
        if(a.getType() == ColumnType.NUMBER && b.getType() == ColumnType.NUMBER) {
            return columnApplyNumber(a, b);
        } else if(a.getType() == ColumnType.STRING && b.getType() == ColumnType.STRING) {
            if(a instanceof EnumColumnView) {
                return columnApplyEnum((EnumColumnView) a, b);
            } else if(b instanceof EnumColumnView) {
                return columnApplyEnum((EnumColumnView) b, a);
            } else {
                return columnApplyString(a, b);
            }
        } else {
            throw new ExprSyntaxException("Comparsion between incompatible types: " + a.getType() + ", " + b.getType());
        }
    }

    private ColumnView columnApplyEnum(EnumColumnView a, ColumnView b) {
        int result[] = new int[a.numRows()];
        for(int i=0; i < result.length; ++i) {
            String xi = a.getId(i);
            String yi = b.getString(i);
            if(xi == null || yi == null) {
                result[i] = ColumnView.NA;
            } else {
                // We can use the comparison result (-1, 0, 1)
                // as in input into the numeric apply() function
                // for example:
                // xi == yi iff xi.compare(yi) == 0
                // xi <= yi iff xi.compare(yi) <= 0
                // etc
                int comparison = xi.compareTo(yi);
                result[i] = apply(comparison, 0) ? 1 : 0;
            }
        }
        return new BooleanColumnView(result);
    }

    private ColumnView columnApplyNumber(ColumnView x, ColumnView y) {
        int result[] = new int[x.numRows()];
        for (int i = 0; i < result.length; i++) {
            double xi = x.getDouble(i);
            double yi = y.getDouble(i);
            if(Double.isNaN(xi) || Double.isNaN(yi)) {
                result[i] = ColumnView.NA;                 
            } else {
                result[i] = apply(xi, yi) ? 1 : 0;
            }
        }
        return new BooleanColumnView(result);
    }

    private ColumnView columnApplyString(ColumnView a, ColumnView b) {
        int result[] = new int[a.numRows()];
        for(int i=0; i < result.length; ++i) {
            String xi = a.getString(i);
            String yi = b.getString(i);
            if(xi == null || yi == null) {
                result[i] = ColumnView.NA;
            } else {
                // We can use the comparison result (-1, 0, 1) 
                // as in input into the numeric apply() function
                // for example:
                // xi == yi iff xi.compare(yi) == 0
                // xi <= yi iff xi.compare(yi) <= 0
                // etc
                int comparison = xi.compareTo(yi);
                result[i] = apply(comparison, 0) ? 1 : 0;
            }
        }
        return new BooleanColumnView(result);
    }

    public final Double extractDouble(FieldValue value) {
        if (value instanceof Quantity) {
            return ((Quantity) value).getValue();
        }
        if (value instanceof LocalDate) {
            return (double) ((LocalDate) value).atMidnightInMyTimezone().getTime();
        }
        return null;
    }

    @Override
    public FieldType resolveResultType(List<FieldType> argumentTypes) {
        return BooleanType.INSTANCE;
    }

    protected abstract boolean apply(FieldValue a, FieldValue b);

    protected abstract boolean apply(double x, double y);
}
