package org.activityinfo.model.expr.simple;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.ExprParser;
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
        SimpleConditionList conditionList = SimpleConditionParser.parse(ExprParser.parse("{Q1717540673}=={t1717155924}"));
        ExprNode formulaRoot = conditionList.toFormula();
        System.out.println(formulaRoot.asExpression());
    }

    @Test
    public void existing() throws IOException {

        URL resource = Resources.getResource(SimpleConditionList.class, "formulas.txt");
        List<String> formulas = Resources.readLines(resource, Charsets.UTF_8);

        for (String formula : formulas) {
            SimpleConditionList conditionList = SimpleConditionParser.parse(ExprParser.parse(formula));
            String reformula = conditionList.toFormula().asExpression();
            ExprNode reparsed;
            try {
                reparsed = ExprParser.parse(reformula);
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
        ExprNode node = ExprParser.parse(formula);
        return SimpleConditionParser.parse(node);
    }

    private FieldValue enumValue(String enumId) {
        return new EnumValue(ResourceId.valueOf(enumId));
    }
}