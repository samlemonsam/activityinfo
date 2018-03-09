package org.activityinfo.store.query.shared;

import org.activityinfo.model.formula.CompoundExpr;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.FormulaParser;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;


public class QueryPathTest {

    @Test
    public void test() {
        assertThat(newQueryPath("Location.Name").path, contains("Location", "Name"));
        assertThat(newQueryPath("Location.Province.Name").path, contains("Location", "Province", "Name"));
    }

    private QueryPath newQueryPath(String expression) {
        FormulaNode expr = FormulaParser.parse(expression);
        return new QueryPath((CompoundExpr) expr);
    }

}