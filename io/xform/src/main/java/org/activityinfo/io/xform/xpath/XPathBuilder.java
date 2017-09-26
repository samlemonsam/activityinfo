package org.activityinfo.io.xform.xpath;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.activityinfo.model.expr.*;
import org.activityinfo.model.expr.diagnostic.ExprException;
import org.activityinfo.model.expr.functions.ExprFunction;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.primitive.BooleanFieldValue;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Builds xpath expressions from AI expressions.
 */
public class XPathBuilder {

    private static final Logger LOGGER = Logger.getLogger(XPathBuilder.class.getName());

    public static final String TRUE = "true()";
    public static final String FALSE = "false()";

    private final XSymbolHandler symbolHandler;

    public XPathBuilder(XSymbolHandler symbolHandler) {
        this.symbolHandler = symbolHandler;
    }

    public String build(String expr) {
        if(Strings.isNullOrEmpty(expr)) {
            return null;
        }
        try {
            return build(ExprParser.parse(expr));
        } catch (ExprException e) {
            LOGGER.log(Level.WARNING, "Could not parse expression '" + expr + "': " + e.getMessage());
            return null;
        } catch (XPathBuilderException e) {
            LOGGER.log(Level.WARNING, "Exception translating expr '" + expr + "' to XPATH: " + e.getMessage());
            return null;
        } catch (XSymbolException e) {
            LOGGER.log(Level.WARNING, "Exception resolving symbol in expr '" + expr + "': " + e.getMessage());
            return null;
        }
    }

    public String build(ExprNode exprNode) {
        if (exprNode == null) {
            return null;
        } else {
            StringBuilder xpath = new StringBuilder();
            appendTo(exprNode, xpath);
            return xpath.toString();
        }
    }

    private void appendTo(ExprNode exprNode, StringBuilder xpath) {
        if (exprNode instanceof ConstantExpr) {
            ConstantExpr constantExpr = (ConstantExpr) exprNode;
            FieldValue value = constantExpr.getValue();

            if (value instanceof BooleanFieldValue) {
                BooleanFieldValue booleanFieldValue = (BooleanFieldValue) value;
                xpath.append(booleanFieldValue.asBoolean() ? TRUE : FALSE);
            } else if (value instanceof EnumValue) {
                String xpathExpr = symbolHandler.resolveSymbol(exprNode);
                xpath.append(xpathExpr);
            } else {
                xpath.append(constantExpr.asExpression());
            }
        } else if (exprNode instanceof FunctionCallNode) {

            FunctionCallNode functionCallNode = (FunctionCallNode) exprNode;
            List<ExprNode> arguments = functionCallNode.getArguments();
            ExprFunction function = functionCallNode.getFunction();

            switch (function.getId()) {
                case "&&":
                    appendBinaryInfixTo("and", arguments, xpath);
                    break;
                case "==":
                    appendBinaryInfixTo("=", arguments, xpath);
                    break;
                case "||":
                    appendBinaryInfixTo("or", arguments, xpath);
                    break;
                case "containsAny":
                case "containsAll":
                case "notContainsAny":
                case "notContainsAll":
                    appendFunction(function.getId(), arguments, xpath);
                    break;
                case"!=":
                case ">":
                case ">=":
                case "<":
                case "<=":
                    appendBinaryInfixTo(function.getId(), arguments, xpath);
                    break;
                case "!":
                    appendUnaryFunction("not", arguments.get(0), xpath);
                    break;
                default:
                    throw new XPathBuilderException("Unsupported function " + function.getId());
            }

        } else if (exprNode instanceof GroupExpr) {
            GroupExpr groupExpr = (GroupExpr) exprNode;
            ExprNode expr = groupExpr.getExpr();
            xpath.append("(");
            appendTo(expr, xpath);
            xpath.append(")");

        } else if (exprNode instanceof SymbolExpr) {
            SymbolExpr symbolExpr = (SymbolExpr) exprNode;

            String xpathExpr = symbolHandler.resolveSymbol(symbolExpr);

            xpath.append(xpathExpr);

        } else if (exprNode instanceof CompoundExpr) {
            CompoundExpr compoundExpr = (CompoundExpr) exprNode;

            List<ExprNode> arguments = new ArrayList<>();
            arguments.add(compoundExpr.getValue());
            arguments.add(compoundExpr.getField());

            appendBinaryInfixTo("=", arguments, xpath);
        } else {
            throw new XPathBuilderException("Unknown expr node " + exprNode);
        }
    }

    private void appendFunction(String functionName, List<ExprNode> arguments, StringBuilder xpath) {
        Preconditions.checkArgument(!arguments.isEmpty());
        Preconditions.checkArgument(arguments.get(0) instanceof SymbolExpr || arguments.get(0) instanceof ConstantExpr);

        switch (functionName) {
            case "containsAny":
                appendSelectedFunction("or", arguments, xpath);
                return;
            case "containsAll":
                appendSelectedFunction("and", arguments, xpath);
                return;
            case "notContainsAny":
                xpath.append("not(");
                appendSelectedFunction("or", arguments, xpath);
                xpath.append(")");
                return;
            case "notContainsAll":
                xpath.append("not(");
                appendSelectedFunction("and", arguments, xpath);
                xpath.append(")");
                return;
        }
        throw new RuntimeException("Function is not supported, function name: " + functionName);
    }

    private void appendSelectedFunction(String joinOperator, List<ExprNode> arguments, StringBuilder xpath) {
        Preconditions.checkArgument(arguments.size() >= 2);
        Preconditions.checkArgument(joinOperator.equals("or") || joinOperator.equals("and"));

        String firstArgXpath = symbolHandler.resolveSymbol(arguments.get(0));

        for (int i = 1; i < arguments.size(); i++) {
            ExprNode argument = arguments.get(i);
            Preconditions.checkState(argument instanceof SymbolExpr || argument instanceof ConstantExpr, "Only symbol/constant expr nodes are supported.");
            String symbol = symbolHandler.resolveSymbol(argument);

            if (argument instanceof SymbolExpr) {
                xpath.append(String.format("selected(%s, %s)", firstArgXpath, symbol));
            } else {
                xpath.append(String.format("selected(%s, %s)", firstArgXpath, symbol));
            }

            if ((i + 1) != arguments.size()) {
                xpath.append(" ").append(joinOperator).append(" ");
            }
        }
    }

    private void appendBinaryInfixTo(String operatorName, List<ExprNode> arguments, StringBuilder xpath) {
        Preconditions.checkArgument(arguments.size() == 2);

        if(isEnumSelectFunction(operatorName, arguments.get(0), arguments.get(1))) {
            appendSelectedFunction("and", arguments, xpath);
            return;
        }

        appendTo(arguments.get(0), xpath);
        switch (operatorName) {
            case "=":
            case "!=":
                xpath.append(operatorName);
                break;
            default:
                xpath.append(" ").append(operatorName).append(" ");
                break;
        }
        appendTo(arguments.get(1), xpath);
    }

    private void appendUnaryFunction(String functionName, ExprNode argument, StringBuilder xpath) {
        Preconditions.checkArgument(argument != null);

        if (argument instanceof GroupExpr) {
            xpath.append(functionName);
            appendTo(argument, xpath);
        } else {
            xpath.append(functionName).append("(");
            appendTo(argument, xpath);
            xpath.append(")");
        }
    }

    private boolean isEnumSelectFunction(String operatorName, ExprNode arg0, ExprNode arg1) {
        if(!operatorName.equals("=")) {
            return false;
        }
        try {
            return symbolHandler.resolveSymbol(arg0) != null && symbolHandler.resolveSymbol(arg1) != null;
        } catch(XSymbolException excp) {
            return false;
        }
    }

    public static String fieldTagName(ResourceId fieldId) {
        return "field_" + fieldId.asString();
    }
}
