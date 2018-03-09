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
package org.activityinfo.model.formula.simple;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.FormulaParser;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.number.Quantity;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class SimpleConditionListTest {


    @Test
    public void containsAll() {
        assertThat(parse("containsAll({Q0396498112},{t0428070508})"), equalTo(
                new SimpleConditionList(
                    Criteria.ALL_TRUE,
                    new SimpleCondition(
                            ResourceId.valueOf("Q0396498112"),
                            SimpleOperator.INCLUDES,
                            new EnumValue(ResourceId.valueOf("t0428070508"))))));
    }

    @Test
    public void containsAny() {
        assertThat(parse("containsAny({Q0428418815},{t0428034066},{t1534594889})"), equalTo(
                new SimpleConditionList(
                    Criteria.ANY_TRUE,
                    new SimpleCondition(
                            ResourceId.valueOf("Q0428418815"),
                            SimpleOperator.INCLUDES,
                            enumValue("t0428034066")),
                    new SimpleCondition(
                            ResourceId.valueOf("Q0428418815"),
                            SimpleOperator.INCLUDES,
                            enumValue("t1534594889"))
                    )));
    }

    @Test
    public void doesNotEqual() {
        assertThat(parse("{i0976213294}!=0"), equalTo(
                new SimpleConditionList(
                    new SimpleCondition(
                            ResourceId.valueOf("i0976213294"),
                            SimpleOperator.NOT_EQUALS,
                            new Quantity(0)))));
    }

    @Test
    public void multipleSingleSelect() {
        assertThat(parse("(Q1180867590==t1208010630)||(Q0250760943==t0131819125)||" +
                        "(Q0454151074==t0021014569)||(Q0506438756==t1742021432)||" +
                        "(Q0889702883==t0196328120)"), equalTo(
                new SimpleConditionList(
                    Criteria.ANY_TRUE,
                    new SimpleCondition(
                        ResourceId.valueOf("Q1180867590"),
                        SimpleOperator.EQUALS,
                        enumValue("t1208010630")),
                    new SimpleCondition(
                        ResourceId.valueOf("Q0250760943"),
                        SimpleOperator.EQUALS,
                        enumValue("t0131819125")),
                    new SimpleCondition(
                        ResourceId.valueOf("Q0454151074"),
                        SimpleOperator.EQUALS,
                        enumValue("t0021014569")),
                    new SimpleCondition(
                        ResourceId.valueOf("Q0506438756"),
                        SimpleOperator.EQUALS,
                        enumValue("t1742021432")),
                    new SimpleCondition(
                        ResourceId.valueOf("Q0889702883"),
                        SimpleOperator.EQUALS,
                        enumValue("t0196328120")))));
    }

    @Test
    public void notContainsAll() {
        assertThat(parse("notContainsAll(Q0007919075,t2044440153,t0008688572,t2051758044)"), equalTo(
                new SimpleConditionList(
                    Criteria.ALL_TRUE,
                    new SimpleCondition(
                        ResourceId.valueOf("Q0007919075"),
                        SimpleOperator.NOT_INCLUDES,
                        enumValue("t2044440153")),
                    new SimpleCondition(
                        ResourceId.valueOf("Q0007919075"),
                        SimpleOperator.NOT_INCLUDES,
                        enumValue("t0008688572")),
                    new SimpleCondition(
                        ResourceId.valueOf("Q0007919075"),
                        SimpleOperator.NOT_INCLUDES,
                        enumValue("t2051758044"))
                    )));
    }

    @Test
    public void singleSelectEqual() {
        SimpleConditionList conditionList = SimpleConditionParser.parse(FormulaParser.parse("{Q1717540673}=={t1717155924}"));
        FormulaNode formulaRoot = conditionList.toFormula();
        System.out.println(formulaRoot.asExpression());
    }

    @Test
    public void numericStringLiteral() {
        testReparsing("a==\"23\"","String Literal test failed");
    }

    @Test
    public void enumerationType() {
        testReparsing("(Q2146921422==t1326920547)&&(i1715311196>0)","Enum Literal test failed");
    }

    private void testReparsing(String expression, String errMessage) {
        SimpleConditionList conditionList = SimpleConditionParser.parse(FormulaParser.parse(expression));
        String reformula = conditionList.toFormula().asExpression();
        FormulaNode reparsed = FormulaParser.parse(reformula);
        SimpleConditionList reparsedConditionList = SimpleConditionParser.parse(reparsed);
        if(!conditionList.equals(reparsedConditionList)) {
            System.err.println(expression);
            System.err.println("conditionList: " + conditionList);
            System.err.println("conditionList.toFormula(): " + reformula);
            System.err.println("reparsedConditionList: " + reparsedConditionList);
            throw new AssertionError(errMessage);
        }
    }


    @Test
    public void existing() throws IOException {

        URL resource = Resources.getResource(SimpleConditionList.class, "formulas.txt");
        List<String> formulas = Resources.readLines(resource, Charsets.UTF_8);

        for (String formula : formulas) {
            FormulaNode parsed = FormulaParser.parse(formula);
            SimpleConditionList conditionList = SimpleConditionParser.parse(parsed);
            String reformula = conditionList.toFormula().asExpression();
            FormulaNode reparsed;
            try {
                reparsed = FormulaParser.parse(reformula);
            } catch (Exception e) {
                System.err.println("Original formula: " + formula);
                System.err.println("conditionList: " + conditionList);
                System.err.println("conditionList.toFormula(): " + reformula);

                throw e;
            }
            SimpleConditionList reparsedConditionList = SimpleConditionParser.parse(reparsed);

            if(!conditionList.equals(reparsedConditionList)) {
                System.err.println("Original formula: " + formula);
                System.err.println("conditionList: " + conditionList);
                System.err.println("conditionList.toFormula(): " + reformula);
                System.err.println("reparsedConditionList: " + reparsedConditionList);
                throw new AssertionError("Round trip failed");
            }
        }

    }

    private SimpleConditionList parse(String formula) {
        FormulaNode node = FormulaParser.parse(formula);
        return SimpleConditionParser.parse(node);
    }

    private FieldValue enumValue(String enumId) {
        return new EnumValue(ResourceId.valueOf(enumId));
    }
}