package org.activityinfo.ui.client.table.view;

import com.google.common.collect.Multimap;
import com.sencha.gxt.data.shared.loader.FilterConfig;
import com.sencha.gxt.data.shared.loader.FilterConfigBean;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.type.time.LocalDate;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.activityinfo.model.expr.ExprParser.parse;
import static org.activityinfo.ui.client.table.view.ColumnFilterParser.findConjunctionList;
import static org.activityinfo.ui.client.table.view.ColumnFilterParser.toFormula;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ColumnFilterParserTest {


    public static final SymbolExpr A = new SymbolExpr("A");
    public static final SymbolExpr B = new SymbolExpr("B");
    public static final SymbolExpr C = new SymbolExpr("C");



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
        cfg.setField("Q");
        cfg.setType("string");
        cfg.setComparison("contains");
        cfg.setValue("Bar");

        assertThat(toFormula(A, cfg).asExpression(), equalTo("ISNUMBER(SEARCH(\"Bar\", Q))"));
    }

    @Test
    public void test() {
        FilterConfig c = new FilterConfigBean();
        c.setField("DOB");
        c.setComparison("on");
        c.setType("date");
        c.setValue("1505779200000");

        assertThat(toFormula(A, c).asExpression(), equalTo("DOB == DATE(2017, 9, 19)"));
    }

    @Test
    public void decomposition() {

        assertThat(findConjunctionList(parse("A && B")), contains(A, B));

        assertThat(findConjunctionList(parse("A && B && C")), contains(A, B, C));

        assertThat(findConjunctionList(parse("A && (B && C)")), contains(A, B, C));

        assertThat(findConjunctionList(parse("(A && ((B && (C))))")), contains(A, B, C));
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
    public void roundTrip() {

        FilterConfig config = new FilterConfigBean();
        config.setType("numeric");
        config.setComparison("eq");
        config.setValue("1");

        ExprNode columnExpr = parse("IF(A && B, A * 42 / 3, CEIL(B / 99))");

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