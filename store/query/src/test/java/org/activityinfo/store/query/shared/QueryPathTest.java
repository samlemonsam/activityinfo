package org.activityinfo.store.query.shared;

import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.ExprParser;
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
        ExprNode expr = ExprParser.parse(expression);
        return new QueryPath((CompoundExpr) expr);
    }

}