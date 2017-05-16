package org.activityinfo.model.expr.simple;

import com.google.common.collect.Lists;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SimpleOperators {

    private static final ArrayList<SimpleOperator> EQUALITY_OPERATORS = Lists.newArrayList(
            SimpleOperator.EQUALS,
            SimpleOperator.NOT_EQUALS);

    private static final ArrayList<SimpleOperator> QUANTITY_OPERATORS = Lists.newArrayList(
            SimpleOperator.EQUALS,
            SimpleOperator.NOT_EQUALS,
            SimpleOperator.LESS_THAN,
            SimpleOperator.LESS_THAN_EQUAL,
            SimpleOperator.GREATER_THAN,
            SimpleOperator.GREATER_THAN_EQUAL);

    private static final ArrayList<SimpleOperator> INCLUDE_OPERATORS = Lists.newArrayList(
            SimpleOperator.INCLUDES,
            SimpleOperator.NOT_INCLUDES);

    public static List<SimpleOperator> forType(FieldType type) {
        if(type instanceof TextType) {
            return EQUALITY_OPERATORS;
        } else if(type instanceof QuantityType) {
            return QUANTITY_OPERATORS;
        } else if(type instanceof EnumType) {
            EnumType enumType = (EnumType) type;
            if(enumType.getCardinality() == Cardinality.SINGLE) {
                return EQUALITY_OPERATORS;
            } else {
                return INCLUDE_OPERATORS;
            }
        }
        return Collections.emptyList();
    }

}
