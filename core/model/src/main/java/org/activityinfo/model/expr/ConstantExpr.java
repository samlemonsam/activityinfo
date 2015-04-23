package org.activityinfo.model.expr;

import org.activityinfo.model.expr.eval.EvalContext;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;

import javax.annotation.Nonnull;

public class ConstantExpr extends ExprNode {

    @Nonnull
    private final FieldValue value;
    private final FieldType type;

    public ConstantExpr(@Nonnull FieldValue value, FieldType type) {
        this.value = value;
        this.type = type;
    }

    public ConstantExpr(double value) {
        this(new Quantity(value), new QuantityType());
    }

    public ConstantExpr(boolean value) {
        this(BooleanFieldValue.valueOf(value), BooleanType.INSTANCE);
    }

    public ConstantExpr(String value) {
        this(TextValue.valueOf(value), TextType.INSTANCE);
    }

    public ConstantExpr(Quantity value) {
        this(value, new QuantityType(value.getUnits()));
    }

    public ConstantExpr(TextValue value) {
        this(value, TextType.INSTANCE);
    }

    public static ConstantExpr valueOf(FieldValue value) {
        if(value instanceof TextValue) {
            return new ConstantExpr(((TextValue) value).asString());
        } else if(value instanceof BooleanFieldValue) {
            return new ConstantExpr(value == BooleanFieldValue.TRUE);
        } else if(value instanceof Quantity) {
            return new ConstantExpr(value, new QuantityType(((Quantity) value).getUnits()));
        } else {
            throw new IllegalArgumentException(value.getTypeClass().getId());
        }
    }

    @Override
    public FieldValue evaluate(EvalContext context) {
        return value;
    }

    @Nonnull
    public FieldValue getValue() {
        return value;
    }

    @Override
    public String asExpression() {
        if(value instanceof TextValue) {
            // TODO: Escaping
            return "\"" + value + "\"";
        } else if(value instanceof Quantity) {
            return Double.toString(((Quantity) value).getValue());
        } else if(value instanceof BooleanFieldValue) {
            return ((BooleanFieldValue) value).asBoolean() ? "true" : "false";
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitConstant(this);
    }

    @Override
    public FieldType resolveType(EvalContext context) {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConstantExpr that = (ConstantExpr) o;

        if (!value.equals(that.value)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }


    @Override
    public String toString() {
        return asExpression();
    }

    public FieldType getType() {
        return type;
    }
}
