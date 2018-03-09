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
package org.activityinfo.ui.client.table.view;

import com.google.common.collect.Multimap;
import com.sencha.gxt.data.shared.loader.FilterConfig;
import com.sencha.gxt.data.shared.loader.FilterConfigBean;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.Formulas;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.formula.functions.AndFunction;
import org.activityinfo.model.type.time.LocalDate;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.activityinfo.model.formula.FormulaParser.parse;
import static org.activityinfo.ui.client.table.view.ColumnFilterParser.toFormula;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ColumnFilterParserTest {


    public static final SymbolNode A = new SymbolNode("A");
    public static final SymbolNode B = new SymbolNode("B");
    public static final SymbolNode C = new SymbolNode("C");
    public static final AndFunction AND = AndFunction.INSTANCE;


    @Test
    public void numeric() {
        FilterConfig config = new FilterConfigBean();
        config.setField("A");
        config.setType("numeric");
        config.setComparison("eq");
        config.setValue("42");

        assertThat(toFormula(A, config).asExpression(), equalTo("A == 42"));
    }

    @Test
    public void string() {
        FilterConfig cfg = new FilterConfigBean();
        cfg.setType("string");
        cfg.setComparison("contains");
        cfg.setValue("Bar");

        assertThat(toFormula(A, cfg).asExpression(), equalTo("ISNUMBER(SEARCH(\"Bar\", A))"));
    }

    @Test
    public void list() {
        FilterConfig cfg = new FilterConfigBean();
        cfg.setType("list");
        cfg.setValue("Item1::Item2::Item3");

        assertThat(toFormula(A, cfg).asExpression(), equalTo("A.Item1 || A.Item2 || A.Item3"));
    }

    @Test
    public void test() {
        FilterConfig c = new FilterConfigBean();
        c.setComparison("on");
        c.setType("date");
        c.setValue("1505779200000");

        assertThat(toFormula(A, c).asExpression(), equalTo("A == DATE(2017, 9, 19)"));
    }

    @Test
    public void decomposition() {

        assertThat(Formulas.findBinaryTree(parse("A && B"), AND), contains(A, B));

        assertThat(Formulas.findBinaryTree(parse("A && B && C"), AND), contains(A, B, C));

        assertThat(Formulas.findBinaryTree(parse("A && (B && C)"), AND), contains(A, B, C));

        assertThat(Formulas.findBinaryTree(parse("(A && ((B && (C))))"), AND), contains(A, B, C));
    }

    @Test
    public void simpleTest() {

        ColumnFilterParser parser = new ColumnFilterParser(asList(A));
        Multimap<Integer, FilterConfig> map = parser.parseFilter(parse("A > 50"));

        Collection<FilterConfig> filters = map.get(0);
        assertThat(filters, hasSize(1));

        assertThat(map.get(0), contains(numericFilter("gt", 50)));

        FilterConfig filter = filters.iterator().next();
        assertThat(filter.getType(), equalTo("numeric"));
        assertThat(filter.getComparison(), equalTo("gt"));
        assertThat(filter.getValue(), equalTo("50.0"));
    }

    @Test
    public void range() {

        ColumnFilterParser parser = new ColumnFilterParser(asList(A));
        Multimap<Integer, FilterConfig> map = parser.parseFilter(parse("A > 50 && A < 100"));

        assertThat(map.get(0), containsInAnyOrder(
            numericFilter("gt", 50),
            numericFilter("lt", 100)));
    }


    @Test
    public void twoFields() {

        ColumnFilterParser parser = new ColumnFilterParser(asList(A, B));
        Multimap<Integer, FilterConfig> map = parser.parseFilter(parse("A > 50 && B == 30"));

        assertThat(map.get(0), contains(numericFilter("gt", 50)));
        assertThat(map.get(1), contains(numericFilter("eq", 30)));
    }

    @Test
    public void dateComparison() {
        ColumnFilterParser parser = new ColumnFilterParser(asList(A));
        Multimap<Integer, FilterConfig> map = parser.parseFilter(parse("A == DATE(2017, 1, 1)"));

        Matcher<FilterConfig> equality = Matchers.allOf(
            hasProperty("type", equalTo("date")),
            hasProperty("comparison", equalTo("on")),
            hasProperty("value", equalTo("" + new LocalDate(2017, 1, 1).atMidnightInMyTimezone().getTime())));

        assertThat(map.get(0), contains(equality));
    }

    @Test
    public void stringContains() {
        ColumnFilterParser parser = new ColumnFilterParser(asList(A));
        Multimap<Integer, FilterConfig> map = parser.parseFilter(parse("ISNUMBER(SEARCH('foo', A))"));

        Matcher<FilterConfig> equality = Matchers.allOf(
            hasProperty("type", equalTo("string")),
            hasProperty("comparison", equalTo("contains")),
            hasProperty("value", equalTo("foo")));

        assertThat(map.get(0), contains(equality));
    }

    @Test
    public void parseList() {
        ColumnFilterParser parser = new ColumnFilterParser(asList(A));
        Multimap<Integer, FilterConfig> map = parser.parseFilter(parse("A.X1 || A.X2 || A.Y2"));

        Matcher<FilterConfig> list = Matchers.allOf(
            hasProperty("type", equalTo("list")),
            hasProperty("value", equalTo("X1::X2::Y2")));

        assertThat(map.get(0), contains(list));

    }

    @Test
    public void roundTrip() {

        FilterConfig config = new FilterConfigBean();
        config.setType("numeric");
        config.setComparison("eq");
        config.setValue("1");

        FormulaNode columnExpr = parse("IF(A && B, A * 42 / 3, CEIL(B / 99))");

        String filterFormula = toFormula(columnExpr, config).asExpression();

        ColumnFilterParser parser = new ColumnFilterParser(asList(A, columnExpr));
        Multimap<Integer, FilterConfig> map = parser.parseFilter(parse(filterFormula));

        assertThat(map.get(1), contains(numericFilter("eq", 1)));
    }

    private Matcher<FilterConfig> numericFilter(String comparison, double value) {
        return Matchers.allOf(
            hasProperty("type", equalTo("numeric")),
            hasProperty("comparison", equalTo(comparison)),
            hasProperty("value", equalTo(Double.toString(value))));
    }

}