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
package org.activityinfo.store.query.shared;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formula.*;
import org.activityinfo.model.formula.eval.FormSymbolTable;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.store.query.shared.columns.ColumnFactory;
import org.activityinfo.store.query.shared.columns.IdColumnBuilder;
import org.activityinfo.store.query.shared.columns.RowCountBuilder;
import org.activityinfo.store.query.shared.columns.ViewBuilderFactory;
import org.activityinfo.store.spi.*;

import java.io.IOException;
import java.util.List;

/**
 * Create a FieldObserver for a given expression
 */
public class ExprQueryBuilder {

    private final ColumnFactory columnFactory;
    private final FormClass formClass;
    private final ColumnQueryBuilder queryBuilder;

    private FormSymbolTable symbolTable;

    public ExprQueryBuilder(ColumnFactory columnFactory, FormClass formClass, ColumnQueryBuilder queryBuilder) {
        this.columnFactory = columnFactory;
        this.formClass = formClass;
        this.symbolTable = new FormSymbolTable(formClass);
        this.queryBuilder = queryBuilder;
    }


    public void addExpr(FormulaNode expr, PendingSlot<ColumnView> target) {

        if(queryBuilder instanceof ColumnQueryBuilderV2) {
            addExprV2(expr, target);
        } else {

            FieldType fieldType = computeType(symbolTable, expr);
            CursorObserver<FieldValue> columnBuilder = ViewBuilderFactory.get(columnFactory, target, fieldType);

            addExprV1(expr, columnBuilder);
        }
    }


    private FieldType computeType(FormSymbolTable parent, FormulaNode expr) {
        if(expr instanceof SymbolNode) {
            return parent.resolveSymbol((SymbolNode) expr).getType();

        } else if(expr instanceof FunctionCallNode) {
            FunctionCallNode callNode = (FunctionCallNode) expr;
            List<FieldType> argumentTypes = Lists.newArrayList();
            for (FormulaNode formulaNode : callNode.getArguments()) {
                argumentTypes.add(computeType(parent, formulaNode));
            }
            return callNode.getFunction().resolveResultType(argumentTypes);
        
        } else if(expr instanceof CompoundExpr) {
            CompoundExpr compoundExpr = (CompoundExpr) expr;
            if(isEnumItemReference((CompoundExpr)expr)) {
                return BooleanType.INSTANCE;
            } else {
                return computeType(computeFormClass(parent, compoundExpr.getValue()), compoundExpr.getField());
            }
            
        } else if(expr instanceof ConstantNode) {
            return ((ConstantNode) expr).getType();
     
        } else if(expr instanceof GroupNode) {
            return computeType(parent, ((GroupNode) expr).getExpr());
        
        } else {
            throw new UnsupportedOperationException("expr: " + expr);    
        }
    }

    private boolean isEnumItemReference(CompoundExpr compoundExpr) {
        if(compoundExpr.getValue() instanceof SymbolNode) {
            Optional<FormField> field = symbolTable.tryResolveSymbol((SymbolNode) compoundExpr.getValue());
            if(field.isPresent() && field.get().getType() instanceof EnumType) {
                return true;
            }
        } 
        return false;
    }

    private FormSymbolTable computeFormClass(FormSymbolTable parent, FormulaNode formulaNode) {
        if(formulaNode instanceof SymbolNode) {
            FormField field = parent.resolveSymbol((SymbolNode) formulaNode);
            if(field.getType() instanceof RecordFieldType) {
                RecordFieldType recordFieldType = (RecordFieldType) field.getType();
                FormClass recordFormClass = recordFieldType.getFormClass();
                
                return new FormSymbolTable(recordFormClass);
            } else {
                throw new IllegalStateException(field.getName() + " is not a record type field.");
            }
        } else if(formulaNode instanceof CompoundExpr) {
            CompoundExpr compoundExpr = (CompoundExpr) formulaNode;
            FormSymbolTable child = computeFormClass(parent, compoundExpr.getValue());
            return computeFormClass(child, compoundExpr.getValue());
        } else {
            throw new UnsupportedOperationException("exprNode: " + formulaNode);
        }
    }
    
    private void addExprV1(FormulaNode expr, CursorObserver<FieldValue> target) {

        if (expr instanceof SymbolNode) {

            SymbolNode symbol = (SymbolNode) expr;
            FormField field = symbolTable.resolveSymbol(symbol);

            queryBuilder.addField(field.getId(), target);

        } else if (expr instanceof FunctionCallNode) {

            FunctionCallNode call = (FunctionCallNode) expr;
            if(call.getArguments().isEmpty()) {



            } else {

                List<CursorObserver<FieldValue>> argumentObservers =
                        CursorObservers.collect(target, call.getArguments().size(), call.getFunction());

                for (int i = 0; i < call.getArgumentCount(); i++) {
                    addExprV1(call.getArgument(i), argumentObservers.get(i));
                }
            }

        } else if (expr instanceof CompoundExpr) {

            CompoundExpr compoundExpr = (CompoundExpr) expr;
            Function<FieldValue, FieldValue> reader;
            if(isEnumItemReference(compoundExpr)) {
               reader = new EnumItemReader(compoundExpr.getField().asResourceId());         
            } else {
                reader = new ComponentReader(compoundExpr.getField().getName());
            }
            CursorObserver<FieldValue> valueObserver = CursorObservers.transform(reader, target);
            addExprV1(compoundExpr.getValue(), valueObserver);

        } else if(expr instanceof ConstantNode) {

            ConstantNode constantNode = (ConstantNode) expr;
            Function<Object, FieldValue> constantFunction = Functions.constant(constantNode.getValue());

            CursorObserver<ResourceId> rowObserver = CursorObservers.transform(constantFunction, target);

            queryBuilder.addResourceId(rowObserver);

        } else if(expr instanceof GroupNode) {
            addExprV1(((GroupNode) expr).getExpr(), target);
            
        } else {
            throw new UnsupportedOperationException("TODO: " + expr);
        }
    }

    private void addExprV2(FormulaNode expr, PendingSlot<ColumnView> target) {

        ColumnQueryBuilderV2 qb = (ColumnQueryBuilderV2) queryBuilder;

        if (expr instanceof SymbolNode) {
            SymbolNode symbol = (SymbolNode) expr;
            FormField field = symbolTable.resolveSymbol(symbol);

            qb.addField(field.getId(), target);

        } else if (expr instanceof FunctionCallNode) {

            throw new UnsupportedOperationException("TODO");


        } else if (expr instanceof CompoundExpr) {

            CompoundExpr compoundExpr = (CompoundExpr) expr;
            ResourceId fieldId = compoundExpr.getField().asResourceId();
            ResourceId enumId = ((SymbolNode) compoundExpr.getValue()).asResourceId();

            if(isEnumItemReference(compoundExpr)) {
                qb.addEnumItem(fieldId, enumId, target);
            } else {
                qb.addFieldComponent(fieldId, enumId, target);
            }

        } else if(expr instanceof ConstantNode) {

            throw new UnsupportedOperationException("TODO");

        } else if(expr instanceof GroupNode) {
            addExprV2(((GroupNode) expr).getExpr(), target);

        } else {
            throw new UnsupportedOperationException("TODO: " + expr);
        }
    }


    public void addField(ResourceId resourceId, CursorObserver<FieldValue> foreignKeyBuilder) {
        queryBuilder.addField(resourceId, foreignKeyBuilder);
    }


    public void addResourceId(PendingSlot<ColumnView> target) {
        if(queryBuilder instanceof ColumnQueryBuilderV2) {
            ((ColumnQueryBuilderV2) queryBuilder).addRecordId(target);
        } else {
            queryBuilder.addResourceId(new IdColumnBuilder(target));
        }
    }

    public void execute() throws IOException {
        queryBuilder.execute();
    }

    public void addRowCount(PendingSlot<Integer> rowCount) {
        if(queryBuilder instanceof ColumnQueryBuilderV2) {
            ((ColumnQueryBuilderV2) queryBuilder).addRowCount(rowCount);
        } else {
            queryBuilder.addResourceId(new RowCountBuilder(rowCount));
        }
    }

    private class ComponentReader implements Function<FieldValue, FieldValue> {

        private String fieldId;

        public ComponentReader(String fieldId) {
            this.fieldId = fieldId;
        }

        @Override
        public FieldValue apply(FieldValue input) {
            if(input instanceof RecordFieldValue) {
                return ((RecordFieldValue) input).getField(fieldId);
            } else {
                return NullFieldValue.INSTANCE;
            }
        }
    }
    
    private class EnumItemReader implements Function<FieldValue, FieldValue> {

        private ResourceId resourceId;

        public EnumItemReader(ResourceId resourceId) {
            this.resourceId = resourceId;
        }

        @Override
        public FieldValue apply(FieldValue input) {
            if(input instanceof EnumValue) {
                EnumValue value = (EnumValue) input;
                if(value.getResourceIds().contains(resourceId)) {
                    return BooleanFieldValue.TRUE;
                }
            }
            return BooleanFieldValue.FALSE;
        }
    }
    
}