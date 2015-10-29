package org.activityinfo.server.endpoint.odk.build;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.activityinfo.model.expr.*;
import org.activityinfo.model.expr.functions.ExprFunction;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.server.endpoint.odk.OdkField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Builds xpath expressions from AI expressions.
 */
public class XPathBuilder {

    private static final Logger LOGGER = Logger.getLogger(XPathBuilder.class.getName());

    public static final String TRUE = "true()";
    public static final String FALSE = "false()";

    private final Map<String, String> symbolMap = new HashMap<>();

    public XPathBuilder(List<OdkField> fields) {
        for(OdkField field : fields) {
            symbolMap.put(field.getModel().getId().asString(), field.getAbsoluteFieldName());
            if(field.getModel().getType() instanceof EnumType) {
                EnumType type = (EnumType) field.getModel().getType();
                for (EnumItem item : type.getValues()) {
                    symbolMap.put(item.getId().asString(), quote(item.getId().asString()));
                }
            }
        }
    }

    private String quote(String value) {
        return "'" + value + "'";
    }

    public String build(String expr) {
        if(Strings.isNullOrEmpty(expr)) {
            return null;
        }
        try {
            return build(ExprParser.parse(expr));
        } catch (XPathBuilderException e) {
            LOGGER.log(Level.WARNING, "Exception translating expr '" + expr + "' to XPATH: " + e.getMessage());
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
                    appendFunction(function.getId(), arguments, xpath);
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

            String xpathExpr = resolveSymbol(symbolExpr);

            xpath.append(xpathExpr);

        } else {
            throw new XPathBuilderException("Unknown expr node " + exprNode);
        }
    }

    private String resolveSymbol(SymbolExpr symbolExpr) {
        String xpath = symbolMap.get(symbolExpr.getName());
        if (xpath == null) {
            throw new XPathBuilderException("Unknown symbol '" + symbolExpr.getName() + "'");
        }
        return xpath;
    }

    private void appendFunction(String functionName, List<ExprNode> arguments, StringBuilder xpath) {
        Preconditions.checkArgument(!arguments.isEmpty());
        Preconditions.checkArgument(arguments.get(0) instanceof SymbolExpr);

        switch (functionName) {
            case "containsAny":
                appendSelectedFunction("or", arguments, xpath);
                return;
            case "containsAll":
                appendSelectedFunction("and", arguments, xpath);
                return;
        }
        throw new RuntimeException("Function is not supported, function name: " + functionName);
    }

    private void appendSelectedFunction(String joinOperator, List<ExprNode> arguments, StringBuilder xpath) {
        Preconditions.checkArgument(arguments.size() >= 2);
        Preconditions.checkArgument(joinOperator.equals("or") || joinOperator.equals("and"));

        String firstArgXpath = resolveSymbol((SymbolExpr) arguments.get(0));

        for (int i = 1; i < arguments.size(); i++) {
            ExprNode argument = arguments.get(i);
            Preconditions.checkState(argument instanceof SymbolExpr, "Only symbol expr nodes are supported.");

            xpath.append(String.format("selected(%s, '%s')", firstArgXpath, ((SymbolExpr)argument).getName()));

            if ((i + 1) != arguments.size()) {
                xpath.append(" ").append(joinOperator).append(" ");
            }
        }
    }

    private void appendBinaryInfixTo(String operatorName, List<ExprNode> arguments, StringBuilder xpath) {
        Preconditions.checkArgument(arguments.size() == 2);
        appendTo(arguments.get(0), xpath);
        xpath.append(" ").append(operatorName).append(" ");
        appendTo(arguments.get(1), xpath);
    }

    public static String fieldTagName(ResourceId fieldId) {
        return "field_" + fieldId.asString();
    }
}
