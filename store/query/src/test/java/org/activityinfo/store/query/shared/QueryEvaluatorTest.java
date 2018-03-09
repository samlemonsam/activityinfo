package org.activityinfo.store.query.shared;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formula.FormulaParser;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.store.query.server.ColumnSetBuilder;
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

        FormStorageProviderStub catalog = new FormStorageProviderStub();
        catalog.addForm(formClass).withRowCount(10);

        ColumnSetBuilder builder = new ColumnSetBuilder(catalog, new NullFormScanCache(), new NullFormSupervisor());
        FormScanBatch batch = builder.createNewBatch();

        QueryEvaluator evaluator = new QueryEvaluator(FilterLevel.BASE, catalog.getTree(formClass.getId()), batch);

        Slot<ColumnView> a = evaluator.evaluateExpression(new SymbolNode("A"));
        Slot<ColumnView> aPlusOne = evaluator.evaluateExpression(FormulaParser.parse("A+1"));

        builder.execute(batch);

        assertThat(a.get().numRows(), equalTo(10));
        assertThat(a.get().getString(0), nullValue());

        assertThat(aPlusOne.get().getString(0), nullValue());
        assertThat(aPlusOne.get().getDouble(0), equalTo(1d));

    }

}