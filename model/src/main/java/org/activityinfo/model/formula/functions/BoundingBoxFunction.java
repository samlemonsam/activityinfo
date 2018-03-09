package org.activityinfo.model.formula.functions;

import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.geo.GeoArea;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;

import java.util.List;

/**
 * Returns min/max lat/lng from a geographic
 */
public abstract class BoundingBoxFunction extends FormulaFunction {


    public static final BoundingBoxFunction XMIN = new BoundingBoxFunction("ST_XMIN") {

        @Override
        protected double apply(GeoArea value) {
            return value.getEnvelope().getX1();
        }
    };

    public static final BoundingBoxFunction XMAX = new BoundingBoxFunction("ST_XMAX") {

        @Override
        protected double apply(GeoArea value) {
            return value.getEnvelope().getX2();
        }
    };

    public static final BoundingBoxFunction YMIN = new BoundingBoxFunction("ST_YMIN") {

        @Override
        protected double apply(GeoArea value) {
            return value.getEnvelope().getY1();
        }
    };

    public static final BoundingBoxFunction YMAX = new BoundingBoxFunction("ST_YMAX") {

        @Override
        protected double apply(GeoArea value) {
            return value.getEnvelope().getY2();
        }
    };


    private String id;

    private BoundingBoxFunction(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getLabel() {
        return id;
    }

    @Override
    public FieldValue apply(List<FieldValue> arguments) {
        checkArity(arguments,1);

        GeoArea value = (GeoArea) arguments.get(0);

        return new Quantity(apply(value));
    }

    protected abstract double apply(GeoArea value);

    @Override
    public FieldType resolveResultType(List<FieldType> argumentTypes) {
        checkArity(argumentTypes,1);

        return new QuantityType("degree");
    }
}
