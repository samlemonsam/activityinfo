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
package org.activityinfo.store.testing;

import org.activityinfo.model.query.ColumnModelException;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.shared.NullFormScanCache;
import org.activityinfo.store.query.shared.NullFormSupervisor;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class UnaryFunctionQueryTest {

    private TestingStorageProvider catalog;
    private ColumnSetBuilder builder;
    private IntakeForm intakeForm;

    @Before
    public void setup() {
        catalog = new TestingStorageProvider();
        builder = new ColumnSetBuilder(catalog, new NullFormScanCache(), new NullFormSupervisor());
        intakeForm = catalog.getIntakeForm();
    }

    @Test
    public void plusFunctionTest() {

        QueryModel queryModel = new QueryModel(intakeForm.getFormId());
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
        QueryModel queryModel = new QueryModel(intakeForm.getFormId());
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

        QueryModel queryModel = new QueryModel(intakeForm.getFormId());
        makeQueryExprAndExpectColumnModelException(queryModel,"*1","multOne");
        makeQueryExprAndExpectColumnModelException(queryModel,"*1.0","multOneDouble");
        makeQueryExprAndExpectColumnModelException(queryModel,"1*","oneMult");
        makeQueryExprAndExpectColumnModelException(queryModel,"1.0*","oneMultDouble");
    }

    @Test
    public void divideFunctionTest() {
        QueryModel queryModel = new QueryModel(intakeForm.getFormId());
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
