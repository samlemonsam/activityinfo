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
package org.activityinfo.model.formula.eval;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formula.*;
import org.activityinfo.model.formula.diagnostic.CircularReferenceException;
import org.activityinfo.model.formula.diagnostic.FormulaException;
import org.activityinfo.model.formula.diagnostic.FormulaSyntaxException;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ErrorType;
import org.activityinfo.model.type.ErrorValue;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.expr.CalculatedFieldType;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Partially evaluates a field-level expression, expanding any calculated indicators
 * on which the expression depends.
 *
 */
public class PartialEvaluator<InstanceT> {

    private final FormSymbolTable symbolTable;
    private final Map<String, FieldReader> readers = Maps.newHashMap();
    private final LinkedList<FormField> stack = new LinkedList<>();
    private final PartiallyEvaluatingVisitor visitor = new PartiallyEvaluatingVisitor();
    private final FieldReaderFactory<InstanceT> readerFactory;

    public PartialEvaluator(@Nonnull FormClass formClass, @Nonnull FieldReaderFactory<InstanceT> readerFactory) {
        this.readerFactory = readerFactory;
        this.symbolTable = new FormSymbolTable(formClass);
    }

    public PartialEvaluator(FormSymbolTable symbolTable, FieldReaderFactory<InstanceT> readerFactory) {
        this.symbolTable = symbolTable;
        this.readerFactory = readerFactory;
    }

    public FieldReader<InstanceT> partiallyEvaluate(FormulaNode node) {
        try {
            return node.accept(visitor);

        } catch(FormulaException e) {
            return new ConstantReader<InstanceT>(new ErrorValue(e), ErrorType.INSTANCE);
        }
    }


    public FieldReader<InstanceT> partiallyEvaluate(FormField field) {
        try {
            return visitor.fieldReader(field);

        } catch(FormulaException e) {
            return new ConstantReader<InstanceT>(new ErrorValue(e), ErrorType.INSTANCE);
        }
    }

    public FormField getField(ResourceId fieldId) {
        return symbolTable.resolveFieldById(fieldId.asString());
    }

    public FormField getField(String fieldName) {
        return symbolTable.resolveFieldById(fieldName);
    }


    private static class ConstantReader<InstanceT> implements FieldReader<InstanceT> {

        private final FieldValue value;
        private final FieldType type;

        private ConstantReader(FieldValue value, FieldType type) {
            this.value = value;
            this.type = type;
        }

        @Override
        public FieldValue readField(InstanceT record) {
            return value;
        }

        @Override
        public FieldType getType() {
            return type;
        }
    }

    private class PartiallyEvaluatingVisitor implements FormulaVisitor<FieldReader<InstanceT>> {


        @Override
        public FieldReader<InstanceT> visitConstant(ConstantNode node) {
            final FieldValue constantValue = node.getValue();
            final FieldType type = node.getType();
            return new FieldReader<InstanceT>() {
                @Override
                public FieldValue readField(InstanceT record) {
                    return constantValue;
                }

                @Override
                public FieldType getType() {
                    return type;
                }
            };
        }

        @Override
        public FieldReader<InstanceT> visitSymbol(SymbolNode symbol) {
            return fieldReader(symbolTable.resolveSymbol(symbol));
        }

        public FieldReader<InstanceT> fieldReader(FormField field) {
            // have we already created a FieldReader for this field name?
            FieldReader reader = readers.get(field.getName());
            if (reader != null) {
                return reader;
            }
            if (field.getType() instanceof CalculatedFieldType) {
                // expand this expression
                reader = expandCalculatedField(field);
            } else {
                reader = readerFactory.create(field);
            }

            // cache partial evaluation
            readers.put(field.getName(), reader);

            return reader;
        }

        private FieldReader<InstanceT> expandCalculatedField(FormField field) {
            // detect cycles
            if (stack.contains(field)) {
                throw new CircularReferenceException(stack);
            }
            stack.add(field);
            try {
                CalculatedFieldType calculatedType = (CalculatedFieldType) field.getType();
                FormulaNode calculatedNode = FormulaParser.parse(calculatedType.getExpression());
                return calculatedNode.accept(this);

            } finally {
                stack.removeLast();
            }
        }

        @Override
        public FieldReader<InstanceT> visitGroup(GroupNode expr) {
            return expr.getExpr().accept(this);
        }

        @Override
        public FieldReader<InstanceT> visitCompoundExpr(CompoundExpr compoundExpr) {
            throw new FormulaSyntaxException("Compound expressions not supported in field-level expressions.");
        }

        @Override
        public FieldReader<InstanceT> visitFunctionCall(final FunctionCallNode functionCallNode) {

            // Partially evaluate arguments
            final List<FieldReader<InstanceT>> arguments = Lists.newArrayList();
            final List<FieldType> argumentTypes = Lists.newArrayList();

            for (FormulaNode argumentExpr : functionCallNode.getArguments()) {
                FieldReader<InstanceT> argumentReader = argumentExpr.accept(this);
                arguments.add(argumentReader);
                argumentTypes.add(argumentReader.getType());
            }

            // Resolve type of the function's value
            final FieldType functionType = functionCallNode.getFunction().resolveResultType(argumentTypes);

            return new FieldReader<InstanceT>() {
                @Override
                public FieldValue readField(InstanceT instance) {
                    List<FieldValue> argumentValues = Lists.newArrayList();
                    for (FieldReader<InstanceT> argument : arguments) {
                        argumentValues.add(argument.readField(instance));
                    }
                    return functionCallNode.getFunction().apply(argumentValues);
                }

                @Override
                public FieldType getType() {
                    return functionType;
                }
            };
        }
    }
}
