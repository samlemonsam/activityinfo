package org.activityinfo.test.acceptance;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.google.common.html.HtmlEscapers;
import cucumber.api.Scenario;
import org.activityinfo.test.driver.ApiFormatter;

public class CucumberApiFormatter extends ApiFormatter {
    private final Escaper escaper = HtmlEscapers.htmlEscaper();
    private Scenario scenario;

    public CucumberApiFormatter(Scenario scenario) {
        this.scenario = scenario;
    }

    @Override
    public void request(String url, String json) {
        scenario.write("<code><pre>" + escaper.escape(json) + "</pre></code>");
    }
}
