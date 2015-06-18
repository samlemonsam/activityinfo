package org.activityinfo.store.query.impl.eval;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import org.activityinfo.model.expr.*;
import org.activityinfo.model.expr.diagnostic.SymbolNotFoundException;
import org.activityinfo.model.expr.eval.FormTreeSymbolTable;
import org.activityinfo.model.expr.eval.SymbolBinding;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.expr.ExprValue;
import org.activityinfo.store.query.impl.CollectionScanBatch;
import org.activityinfo.store.query.impl.Slot;
import org.activityinfo.store.query.impl.views.ColumnFilter;

import java.util.List;
import java.util.logging.Logger;

/**
 * Constructs a set of rows from a given RowSource model.
 *
 * Several row sources may combine to form a single logical table.
 */
public class QueryEvaluator {
    private static final Logger LOGGER = Logger.getLogger(QueryEvaluator.class.getName());

    private CollectionScanBatch batch;
    private FormTree tree;
    private FormClass rootFormClass;

    private final ColumnExprVisitor columnVisitor = new ColumnExprVisitor();

    private FormTreeSymbolTable resolver;

    public QueryEvaluator(FormTree formTree, FormClass rootFormClass, CollectionScanBatch batch) {
        this.tree = formTree;
        this.resolver = new FormTreeSymbolTable(formTree);
        this.rootFormClass = rootFormClass;
        this.batch = batch;
    }

    public Slot<ColumnView> evaluateExpression(String expression) {
        ExprNode expr = ExprParser.parse(expression);
        try {
            return expr.accept(columnVisitor);

        } catch(SymbolNotFoundException e) {
            throw new SymbolNotFoundException(expression);
        }
    }

    public Function<ColumnView, ColumnView> filter(ExprValue filter) {
        if(filter == null) {
            return Functions.identity();
        } else {
            return new ColumnFilter(evaluateExpression(filter.getExpression()));
        }
    }

    public Slot<ColumnView> addField(FormField field) {
        return batch.addColumn(tree.getRootField(field.getId()));
    }

    private class ColumnExprVisitor implements ExprVisitor<Slot<ColumnView>> {

        @Override
        public Slot<ColumnView> visitConstant(ConstantExpr node) {
            return batch.addConstantColumn(rootFormClass, node.getValue());
        }

        @Override
        public Slot<ColumnView> visitSymbol(SymbolExpr symbolExpr) {
            if(symbolExpr.getName().equals(ColumnModel.ID_SYMBOL)) {
                return batch.addResourceIdColumn(rootFormClass);

            } else if(symbolExpr.getName().equals(ColumnModel.CLASS_SYMBOL)) {
                return batch.addConstantColumn(rootFormClass, rootFormClass.getId().asString());
            }

            SymbolBinding fieldMatch = resolver.resolveSymbol(symbolExpr);
            return batch.addColumn(fieldMatch.getField());
        }

        @Override
        public Slot<ColumnView> visitGroup(GroupExpr group) {
            return group.getExpr().accept(this);
        }

        @Override
        public Slot<ColumnView> visitCompoundExpr(CompoundExpr compoundExpr) {
            SymbolBinding binding = resolver.resolveCompoundExpr(tree.getRootFields(), compoundExpr);
            LOGGER.info("Resolved expr '" + compoundExpr + "' to " + binding.getField().debugPath());
            return batch.addColumn(binding.getField());
        }

        @Override
        public Slot<ColumnView> visitFunctionCall(final FunctionCallNode call) {
            if(ColumnFunctions.isSupported(call.getFunction())) {
                final List<Slot<ColumnView>> arguments = Lists.newArrayList();
                for(ExprNode argument : call.getArguments()) {
                    arguments.add(argument.accept(this));
                }
                return new Slot<ColumnView>() {
                    @Override
                    public ColumnView get() {
                        List<ColumnView> columns = Lists.newArrayList();
                        for (Slot<ColumnView> argument : arguments) {
                            columns.add(argument.get());
                        }
                        return ColumnFunctions.create(call.getFunction(), columns);
                    }
                };
            } else {
                return batch.addExpression(rootFormClass, call);
            }
        }
    }
}
