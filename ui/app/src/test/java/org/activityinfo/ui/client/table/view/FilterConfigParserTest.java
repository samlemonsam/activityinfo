package org.activityinfo.ui.client.table.view;

import com.sencha.gxt.data.shared.loader.FilterConfig;
import com.sencha.gxt.data.shared.loader.FilterConfigBean;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class FilterConfigParserTest {

    @Test
    public void numeric() {
        FilterConfig config = new FilterConfigBean();
        config.setField("A");
        config.setType("numeric");
        config.setComparison("eq");
        config.setValue("42");

        assertThat(FilterConfigParser.toFormula(config).asExpression(), equalTo("A == 42"));
    }

    @Test
    public void string() {
        FilterConfig cfg = new FilterConfigBean();
        cfg.setField("Q");
        cfg.setType("string");
        cfg.setComparison("contains");
        cfg.setValue("Bar");

        assertThat(FilterConfigParser.toFormula(cfg).asExpression(), equalTo("ISNUMBER(SEARCH(\"Bar\", Q))"));
    }

    @Test
    public void test() {
        FilterConfig c = new FilterConfigBean();
        c.setField("DOB");
        c.setComparison("on");
        c.setType("date");
        c.setValue("1505779200000");

        assertThat(FilterConfigParser.toFormula(c).asExpression(), equalTo("DOB == DATE(2017, 9, 19)"));
    }

}