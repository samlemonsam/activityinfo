package org.activityinfo.model.expr.simple;

import com.google.common.collect.Iterables;
import org.activityinfo.model.expr.*;
import org.activityinfo.model.expr.functions.*;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.primitive.TextValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by alex on 15-5-17.
 */
public class SimpleConditionParser {

    public static SimpleConditionList parse(ExprNode rootNode) {
        if(rootNode instanceof FunctionCallNode) {
            FunctionCallNode call = (FunctionCallNode) rootNode;
            if(call.getFunction() == ContainsAllFunction.INSTANCE) {
                return contains(Criteria.ALL_TRUE, call, SimpleOperator.INCLUDES);
            } else if(call.getFunction() == ContainsAnyFunction.INSTANCE) {
                return contains(Criteria.ANY_TRUE, call, SimpleOperator.INCLUDES);
            } else if(call.getFunction() == NotContainsAllFunction.INSTANCE) {
                return contains(Criteria.ALL_TRUE, call, SimpleOperator.NOT_INCLUDES);
            }
        }

        List<SimpleCondition> conditions = new ArrayList<>();
        Set<Criteria> criteria = new HashSet<>();

        parse(conditions, criteria, rootNode);

        if(conditions.size() == 1) {
            return new SimpleConditionList(conditions.get(0));
        }

        if(criteria.size() != 1) {
            throw new UnsupportedOperationException("mixing and/or: " + rootNode);
        }

        return new SimpleConditionList(Iterables.getOnlyElement(criteria), conditions);

    }

    private static void parse(List<SimpleCondition> conditions, Set<Criteria> criteria, ExprNode node) {
        if(node instanceof GroupExpr) {
            parse(conditions, criteria, ((GroupExpr) node).getExpr());
            return;
        }

        if(node instanceof FunctionCallNode) {
            FunctionCallNode call = (FunctionCallNode) node;
            if (call.getFunction() == AndFunction.INSTANCE) {
                criteria.add(Criteria.ALL_TRUE);
                parse(conditions, criteria, call.getArgument(0));
                parse(conditions, criteria, call.getArgument(1));
                return;

            } else if (call.getFunction() == OrFunction.INSTANCE) {
                criteria.add(Criteria.ANY_TRUE);
                parse(conditions, criteria, call.getArgument(0));
                parse(conditions, criteria, call.getArgument(1));
                return;
            }
        }

        conditions.add(parseCondition(node));
    }

    private static SimpleCondition parseCondition(ExprNode node) {
        if(node instanceof GroupExpr) {
            return parseCondition(((GroupExpr) node).getExpr());
        } else if(node instanceof FunctionCallNode) {
            FunctionCallNode callNode = ((FunctionCallNode) node);
            if(callNode.getFunction() == NotFunction.INSTANCE) {
                return parseCondition(callNode.getArgument(0)).negate();
            } else if(callNode.getArguments().size() == 2) {
                return parseBinary(((FunctionCallNode) node));
            } else {
                throw new UnsupportedOperationException("function: " + ((FunctionCallNode) node).getFunction());
            }
        } else if(node instanceof CompoundExpr) {
            return parseEnumCondition((CompoundExpr) node);

        } else {
            throw new UnsupportedOperationException("cannot handle expression: " + node);
        }
    }

    private static SimpleCondition parseEnumCondition(CompoundExpr node) {
        ResourceId fieldId = parseFieldId(node.getValue());
        ResourceId enumItem = node.getField().asResourceId();

        return new SimpleCondition(fieldId, SimpleOperator.INCLUDES, new EnumValue(enumItem));
    }

    private static SimpleCondition parseBinary(FunctionCallNode call) {
        ResourceId fieldId = parseFieldId(call.getArgument(0));
        SimpleOperator op = parseOp(call.getFunction());
        FieldValue fieldValue = parseFieldValue(call.getArgument(1));

        return new SimpleCondition(fieldId, op, fieldValue);
    }

    private static FieldValue parseFieldValue(ExprNode argument) {
        if(argument instanceof SymbolExpr) {
            ResourceId id = ResourceId.valueOf(((SymbolExpr) argument).getName());
            if(id.getDomain() == CuidAdapter.ATTRIBUTE_DOMAIN) {
                return new EnumValue(id);
            } else {
                return TextValue.valueOf(id.asString());
            }
        } else if(argument instanceof ConstantExpr) {
            return ((ConstantExpr) argument).getValue();
        }
        throw new UnsupportedOperationException("constant value: " + argument);
    }

    private static SimpleOperator parseOp(ExprFunction function) {
        if(function == EqualFunction.INSTANCE) {
            return SimpleOperator.EQUALS;
        } else if(function == NotEqualFunction.INSTANCE) {
            return SimpleOperator.NOT_EQUALS;
        } else if(function == LessFunction.INSTANCE) {
            return SimpleOperator.LESS_THAN;
        } else if(function == LessOrEqualFunction.INSTANCE) {
            return SimpleOperator.LESS_THAN_EQUAL;
        } else if(function == GreaterFunction.INSTANCE) {
            return SimpleOperator.GREATER_THAN;
        } else if(function == GreaterOrEqualFunction.INSTANCE) {
            return SimpleOperator.GREATER_THAN_EQUAL;
        } else {
            throw new UnsupportedOperationException("function: " + function);
        }
    }

    private static SimpleConditionList contains(Criteria criteria, FunctionCallNode call, SimpleOperator operator) {
        ResourceId field = parseFieldId(call.getArgument(0));

        List<SimpleCondition> conditions = new ArrayList<>();
        for (int i = 1; i < call.getArguments().size(); i++) {
            conditions.add(new SimpleCondition(field, operator, parseEnum(call.getArgument(i))));
        }

        return new SimpleConditionList(criteria, conditions);
    }


    private static ResourceId parseFieldId(ExprNode argument) {
        if(argument instanceof SymbolExpr) {
            return ResourceId.valueOf(((SymbolExpr) argument).getName());
        }
        throw new IllegalArgumentException("Expected symbol: " + argument);
    }


    private static FieldValue parseEnum(ExprNode argument) {
        if(argument instanceof SymbolExpr) {
            return new EnumValue(ResourceId.valueOf(((SymbolExpr) argument).getName()));
        } else if(argument instanceof ConstantExpr) {
            if(((ConstantExpr) argument).getType() instanceof EnumType) {
                return ((ConstantExpr) argument).getValue();
            }
            else {
                return new EnumValue(ResourceId.valueOf(((ConstantExpr) argument).asExpression()));
            }
        }
        throw new IllegalArgumentException("Expected symbol: " + argument);
    }
}
