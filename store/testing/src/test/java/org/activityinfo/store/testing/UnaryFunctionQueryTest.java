package org.activityinfo.store.testing;

import org.activityinfo.model.query.ColumnModelException;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.store.query.impl.ColumnSetBuilder;
import org.activityinfo.store.query.impl.NullFormScanCache;
import org.activityinfo.store.query.impl.NullFormSupervisor;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class UnaryFunctionQueryTest {

    @Test
    public void plusFunctionTest() {
        TestingCatalog catalog = new TestingCatalog();
        ColumnSetBuilder builder = new ColumnSetBuilder(catalog, new NullFormScanCache(), new NullFormSupervisor());

        QueryModel queryModel = new QueryModel(IntakeForm.FORM_ID);
        queryModel.selectExpr("+1").as("posOne");
        queryModel.selectExpr("+1.0").as("posOneDouble");
        queryModel.selectExpr("+1*2").as("posOneByTwo");
        queryModel.selectExpr("+1.0*2.0").as("posOneByTwoDouble");
        queryModel.selectExpr("2*(+1)").as("twoByPosOne");
        queryModel.selectExpr("2.0*(+1.0)").as("twoByPosOneDouble");

        ColumnSet columnSet = builder.build(queryModel);
        ColumnView posOne = columnSet.getColumnView("posOne");
        ColumnView posOneDouble = columnSet.getColumnView("posOneDouble");
        ColumnView posOneByTwo = columnSet.getColumnView("posOneByTwo");
        ColumnView posOneByTwoDouble = columnSet.getColumnView("posOneByTwoDouble");
        ColumnView twoByPosOne = columnSet.getColumnView("twoByPosOne");
        ColumnView twoByPosOneDouble = columnSet.getColumnView("twoByPosOneDouble");

        assertThat(posOne.numRows(), equalTo(IntakeForm.ROW_COUNT));
        assertThat(posOne.getDouble(0), equalTo(1.0));
        assertThat(posOneDouble.getDouble(0), equalTo(1.0));
        assertThat(posOneByTwo.getDouble(0), equalTo(2.0));
        assertThat(posOneByTwoDouble.getDouble(0), equalTo(2.0));
        assertThat(twoByPosOne.getDouble(0), equalTo(2.0));
        assertThat(twoByPosOneDouble.getDouble(0), equalTo(2.0));
    }

    @Test
    public void minusFunctionTest() {
        TestingCatalog catalog = new TestingCatalog();
        ColumnSetBuilder builder = new ColumnSetBuilder(catalog, new NullFormScanCache(), new NullFormSupervisor());

        QueryModel queryModel = new QueryModel(IntakeForm.FORM_ID);
        queryModel.selectExpr("-1").as("negOne");
        queryModel.selectExpr("-1.0").as("negOneDouble");
        queryModel.selectExpr("-1*2").as("negOneByTwo");
        queryModel.selectExpr("-1.0*2.0").as("negOneByTwoDouble");
        queryModel.selectExpr("2*(-1)").as("twoByNegOne");
        queryModel.selectExpr("2.0*(-1.0)").as("twoByNegOneDouble");

        ColumnSet columnSet = builder.build(queryModel);
        ColumnView negOne = columnSet.getColumnView("negOne");
        ColumnView negOneDouble = columnSet.getColumnView("negOneDouble");
        ColumnView negOneByTwo = columnSet.getColumnView("negOneByTwo");
        ColumnView negOneByTwoDouble = columnSet.getColumnView("negOneByTwoDouble");
        ColumnView twoByNegOne = columnSet.getColumnView("twoByNegOne");
        ColumnView twoByNegOneDouble = columnSet.getColumnView("twoByNegOneDouble");

        assertThat(negOne.numRows(), equalTo(IntakeForm.ROW_COUNT));
        assertThat(negOne.getDouble(0), equalTo(-1.0));
        assertThat(negOneDouble.getDouble(0), equalTo(-1.0));
        assertThat(negOneByTwo.getDouble(0), equalTo(-2.0));
        assertThat(negOneByTwoDouble.getDouble(0), equalTo(-2.0));
        assertThat(twoByNegOne.getDouble(0), equalTo(-2.0));
        assertThat(twoByNegOneDouble.getDouble(0), equalTo(-2.0));
    }

    @Test
    public void multiplyFunctionTest() {
        TestingCatalog catalog = new TestingCatalog();
        ColumnSetBuilder builder = new ColumnSetBuilder(catalog, new NullFormScanCache(), new NullFormSupervisor());

        QueryModel queryModel = new QueryModel(IntakeForm.FORM_ID);
        makeQueryExprAndExpectColumnModelException(queryModel,"*1","multOne");
        makeQueryExprAndExpectColumnModelException(queryModel,"*1.0","multOneDouble");
        makeQueryExprAndExpectColumnModelException(queryModel,"1*","oneMult");
        makeQueryExprAndExpectColumnModelException(queryModel,"1.0*","oneMultDouble");
    }

    @Test
    public void divideFunctionTest() {
        TestingCatalog catalog = new TestingCatalog();
        ColumnSetBuilder builder = new ColumnSetBuilder(catalog, new NullFormScanCache(), new NullFormSupervisor());

        QueryModel queryModel = new QueryModel(IntakeForm.FORM_ID);
        makeQueryExprAndExpectColumnModelException(queryModel,"/1","divOne");
        makeQueryExprAndExpectColumnModelException(queryModel,"/1.0","divOneDouble");
        makeQueryExprAndExpectColumnModelException(queryModel,"1/","oneDiv");
        makeQueryExprAndExpectColumnModelException(queryModel,"1.0/","oneDivDouble");
    }

    private void makeQueryExprAndExpectColumnModelException(QueryModel model, String expression, String exprName) {
        try {
            model.selectExpr(expression).as(exprName);
            //assertThat(field.getDouble(0),equalTo(Double.NaN));
            throw new AssertionError("Input \"" + expression.toString() + "\" expected to cause ColumnModelException");
        } catch(ColumnModelException excp) { /* Expected Exception */ }
    }

}
