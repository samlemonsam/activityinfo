package org.activityinfo.store.query.impl.eval;

import org.activityinfo.model.expr.ExprParser;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.store.query.impl.CollectionScanBatch;
import org.activityinfo.store.query.impl.Slot;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;


public class QueryEvaluatorTest {

    @Test
    public void circularReference() throws Exception {


        final FormClass formClass = new FormClass(ResourceId.valueOf("XYZ"));
        formClass.addField(ResourceId.valueOf("FA"))
                .setCode("A")
                .setLabel("Field A")
                .setType(new CalculatedFieldType("B"));
        formClass.addField(ResourceId.valueOf("FB"))
                .setCode("B")
                .setLabel("Field B")
                .setType(new CalculatedFieldType("A"));

        CatalogStub catalog = new CatalogStub();
        catalog.addForm(formClass).withRowCount(10);

        CollectionScanBatch batch = new CollectionScanBatch(catalog);
        QueryEvaluator evaluator = new QueryEvaluator(catalog.getTree(formClass.getId()), formClass, batch);

        Slot<ColumnView> a = evaluator.evaluateExpression(new SymbolExpr("A"));
        Slot<ColumnView> aPlusOne = evaluator.evaluateExpression(ExprParser.parse("A+1"));

        batch.execute();

        assertThat(a.get().numRows(), equalTo(10));
        assertThat(a.get().getString(0), nullValue());

        assertThat(aPlusOne.get().getString(0), nullValue());
        assertThat(aPlusOne.get().getDouble(0), equalTo(1d));

    }

}