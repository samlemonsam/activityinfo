/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.io.xform.xpath;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.activityinfo.model.formula.*;
import org.activityinfo.model.formula.diagnostic.FormulaException;
import org.activityinfo.model.formula.functions.FormulaFunction;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumType;
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
            return build(FormulaParser.parse(expr));
        } catch (FormulaException e) {
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

    public String build(FormulaNode formulaNode) {
        if (formulaNode == null) {
            return null;
        } else {
            StringBuilder xpath = new StringBuilder();
            appendTo(formulaNode, xpath);
            return xpath.toString();
        }
    }

    private void appendTo(FormulaNode formulaNode, StringBuilder xpath) {
        if (formulaNode instanceof ConstantNode) {
            ConstantNode constantNode = (ConstantNode) formulaNode;
            FieldValue value = constantNode.getValue();

            if (value instanceof BooleanFieldValue) {
                BooleanFieldValue booleanFieldValue = (BooleanFieldValue) value;
                xpath.append(booleanFieldValue.asBoolean() ? TRUE : FALSE);
            } else if (value instanceof EnumValue) {
                String xpathExpr = resolveSymbol(formulaNode);
                xpath.append(xpathExpr);
            } else {
                xpath.append(constantNode.asExpression());
            }
        } else if (formulaNode instanceof FunctionCallNode) {

            FunctionCallNode functionCallNode = (FunctionCallNode) formulaNode;
            List<FormulaNode> arguments = functionCallNode.getArguments();
            FormulaFunction function = functionCallNode.getFunction();

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

        } else if (formulaNode instanceof GroupNode) {
            GroupNode groupNode = (GroupNode) formulaNode;
            FormulaNode expr = groupNode.getExpr();
            xpath.append("(");
            appendTo(expr, xpath);
            xpath.append(")");

        } else if (formulaNode instanceof SymbolNode) {
            SymbolNode symbolNode = (SymbolNode) formulaNode;

            String xpathExpr = resolveSymbol(symbolNode);

            xpath.append(xpathExpr);

        } else if (formulaNode instanceof CompoundExpr) {
            CompoundExpr compoundExpr = (CompoundExpr) formulaNode;

            List<FormulaNode> arguments = new ArrayList<>();
            arguments.add(compoundExpr.getValue());
            arguments.add(compoundExpr.getField());

            appendBinaryInfixTo("=", arguments, xpath);
        } else {
            throw new XPathBuilderException("Unknown expr node " + formulaNode);
        }
    }

    private void appendFunction(String functionName, List<FormulaNode> arguments, StringBuilder xpath) {
        Preconditions.checkArgument(!arguments.isEmpty());
        Preconditions.checkArgument(arguments.get(0) instanceof SymbolNode || arguments.get(0) instanceof ConstantNode);

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

    private void appendSelectedFunction(String joinOperator, List<FormulaNode> arguments, StringBuilder xpath) {
        Preconditions.checkArgument(arguments.size() >= 2);
        Preconditions.checkArgument(joinOperator.equals("or") || joinOperator.equals("and"));

        String firstArgXpath = resolveSymbol(arguments.get(0));

        for (int i = 1; i < arguments.size(); i++) {
            FormulaNode argument = arguments.get(i);
            Preconditions.checkState(argument instanceof SymbolNode || argument instanceof ConstantNode, "Only symbol/constant expr nodes are supported.");
            String symbol = resolveSymbol(argument);

            if (argument instanceof SymbolNode) {
                xpath.append(String.format("selected(%s, %s)", firstArgXpath, symbol));
            } else {
                xpath.append(String.format("selected(%s, %s)", firstArgXpath, symbol));
            }

            if ((i + 1) != arguments.size()) {
                xpath.append(" ").append(joinOperator).append(" ");
            }
        }
    }

    private void appendBinaryInfixTo(String operatorName, List<FormulaNode> arguments, StringBuilder xpath) {
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

    private void appendUnaryFunction(String functionName, FormulaNode argument, StringBuilder xpath) {
        Preconditions.checkArgument(argument != null);

        if (argument instanceof GroupNode) {
            xpath.append(functionName);
            appendTo(argument, xpath);
        } else {
            xpath.append(functionName).append("(");
            appendTo(argument, xpath);
            xpath.append(")");
        }
    }

    private boolean isEnumSelectFunction(String operatorName, FormulaNode arg0, FormulaNode arg1) {
        if(!operatorName.equals("=")) {
            return false;
        }
        try {
            return resolveSymbol(arg0) != null && resolveSymbol(arg1) != null;
        } catch(XSymbolException excp) {
            return false;
        }
    }

    public static String fieldTagName(ResourceId fieldId) {
        return "field_" + fieldId.asString();
    }

    private String resolveSymbol(FormulaNode formulaNode) throws XSymbolException {
        if (formulaNode instanceof SymbolNode) {
            return resolveSymbol((SymbolNode) formulaNode);
        } else if (formulaNode instanceof ConstantNode) {
            return resolveSymbol((ConstantNode) formulaNode);
        }
        throw new XSymbolException(formulaNode.asExpression());
    }

    private String resolveSymbol(SymbolNode symbolNode) throws XSymbolException {
        return symbolHandler.resolveSymbol(symbolNode.getName());
    }

    private String resolveSymbol(ConstantNode constantNode) throws XSymbolException {
        String resolved;
        if (constantNode.getType() instanceof EnumType) {
            EnumValue enumValue = (EnumValue) constantNode.getValue();
            resolved = symbolHandler.resolveSymbol(enumValue.getValueId().asString());
        } else {
            resolved = symbolHandler.resolveSymbol(constantNode.getValue().toString());
        }
        return resolved;
    }
}
