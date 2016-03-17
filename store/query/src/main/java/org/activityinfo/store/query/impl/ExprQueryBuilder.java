package org.activityinfo.store.query.impl;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.activityinfo.model.expr.*;
import org.activityinfo.model.expr.eval.FormSymbolTable;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.RecordFieldValue;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.NullFieldValue;
import org.activityinfo.model.type.RecordFieldType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.service.store.ColumnQueryBuilder;
import org.activityinfo.service.store.CursorObserver;
import org.activityinfo.service.store.CursorObservers;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.query.impl.builders.ViewBuilderFactory;

import java.io.IOException;
import java.util.List;

/**
 * Create a FieldObserver for a given expression
 */
public class ExprQueryBuilder {

    private final FormClass formClass;
    private final ColumnQueryBuilder queryBuilder;
    
    private FormSymbolTable symbolTable;

    public ExprQueryBuilder(ResourceCollection collection) {
        this.formClass = collection.getFormClass();
        this.queryBuilder = collection.newColumnQuery();
        this.symbolTable = new FormSymbolTable(formClass);
    }

    
    public void addExpr(ExprNode expr, PendingSlot<ColumnView> target) {
        
        FieldType fieldType = computeType(symbolTable, expr);
        CursorObserver<FieldValue> columnBuilder = ViewBuilderFactory.get(target, fieldType);

        addExpr(expr, columnBuilder);
    }

    private FieldType computeType(FormSymbolTable parent, ExprNode expr) {
        if(expr instanceof SymbolExpr) {
            return parent.resolveSymbol((SymbolExpr) expr).getType();

        } else if(expr instanceof FunctionCallNode) {
            FunctionCallNode callNode = (FunctionCallNode) expr;
            List<FieldType> argumentTypes = Lists.newArrayList();
            for (ExprNode exprNode : callNode.getArguments()) {
                argumentTypes.add(computeType(parent, exprNode));
            }
            return callNode.getFunction().resolveResultType(argumentTypes);
        
        } else if(expr instanceof CompoundExpr) {
            CompoundExpr compoundExpr = (CompoundExpr) expr;
            if(isEnumItemReference((CompoundExpr)expr)) {
                return BooleanType.INSTANCE;
            } else {
                return computeType(computeFormClass(parent, compoundExpr.getValue()), compoundExpr.getField());
            }
            
        } else if(expr instanceof ConstantExpr) {
            return ((ConstantExpr) expr).getType();
     
        } else if(expr instanceof GroupExpr) {
            return computeType(parent, ((GroupExpr) expr).getExpr());
        
        } else {
            throw new UnsupportedOperationException("expr: " + expr);    
        }
    }

    private boolean isEnumItemReference(CompoundExpr compoundExpr) {
        if(compoundExpr.getValue() instanceof SymbolExpr) {
            Optional<FormField> field = symbolTable.tryResolveSymbol((SymbolExpr) compoundExpr.getValue());
            if(field.isPresent() && field.get().getType() instanceof EnumType) {
                return true;
            }
        } 
        return false;
    }

    private FormSymbolTable computeFormClass(FormSymbolTable parent, ExprNode exprNode) {
        if(exprNode instanceof SymbolExpr) {
            FormField field = parent.resolveSymbol((SymbolExpr) exprNode);
            if(field.getType() instanceof RecordFieldType) {
                RecordFieldType recordFieldType = (RecordFieldType) field.getType();
                FormClass recordFormClass = recordFieldType.getFormClass();
                
                return new FormSymbolTable(recordFormClass);
            } else {
                throw new IllegalStateException(field.getName() + " is not a record type field.");
            }
        } else if(exprNode instanceof CompoundExpr) {
            CompoundExpr compoundExpr = (CompoundExpr) exprNode;
            FormSymbolTable child = computeFormClass(parent, compoundExpr.getValue());
            return computeFormClass(child, compoundExpr.getValue());
        } else {
            throw new UnsupportedOperationException("exprNode: " + exprNode);
        }
    }
    
    private void addExpr(ExprNode expr, CursorObserver<FieldValue> target) {

        if (expr instanceof SymbolExpr) {

            SymbolExpr symbol = (SymbolExpr) expr;
            FormField field = symbolTable.resolveSymbol(symbol);

            queryBuilder.addField(field.getId(), target);

        } else if (expr instanceof FunctionCallNode) {
            
            FunctionCallNode call = (FunctionCallNode) expr;
            List<CursorObserver<FieldValue>> argumentObservers =
                    CursorObservers.collect(target, call.getArguments().size(), call.getFunction());

            for (int i = 0; i < call.getArgumentCount(); i++) {
                addExpr(call.getArgument(i), argumentObservers.get(i));
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
            addExpr(compoundExpr.getValue(), valueObserver);

        } else if(expr instanceof ConstantExpr) {

            ConstantExpr constantExpr = (ConstantExpr) expr;
            Function<Object, FieldValue> constantFunction = Functions.constant(constantExpr.getValue());

            CursorObserver<ResourceId> rowObserver = CursorObservers.transform(constantFunction, target);

            queryBuilder.addResourceId(rowObserver);

        } else if(expr instanceof GroupExpr) {
            addExpr(((GroupExpr) expr).getExpr(), target);
            
        } else {
            throw new UnsupportedOperationException("TODO: " + expr);
        }
    }

    public void addField(ResourceId resourceId, CursorObserver<FieldValue> foreignKeyBuilder) {
        queryBuilder.addField(resourceId, foreignKeyBuilder);
    }


    public void addResourceId(CursorObserver<ResourceId> cursorObserver) {
        queryBuilder.addResourceId(cursorObserver);
    }

    public void execute() throws IOException {
        queryBuilder.execute();
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