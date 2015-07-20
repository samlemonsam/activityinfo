package org.activityinfo.test.cucumber;

import com.google.common.base.Throwables;
import gherkin.formatter.Formatter;
import gherkin.formatter.PrettyFormatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;
import org.activityinfo.test.TestReporter;

import java.util.List;

public class ReportingAdapter implements Formatter, Reporter {

    private final TestReporter reporter;
    private StringBuilder output;
    private boolean passed = true;
    private PrettyFormatter formatter;

    public ReportingAdapter(TestReporter reporter) {
        this.reporter = reporter;
        output = new StringBuilder();
    }

    @Override
    public void feature(Feature feature) {
        printlnf("Feature: %s", feature.getName());
    }

    @Override
    public void uri(String uri) {
    }
    
    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
    }


    @Override
    public void background(Background background) {
        output.append("Running Background:\n");
    }
    
    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        printlnf("Starting Scenario Outline: %s", scenarioOutline.getName());
    }

    @Override
    public void examples(Examples examples) {
    }

    @Override
    public void startOfScenarioLifeCycle(Scenario scenario) {
    }

    @Override
    public void scenario(Scenario scenario) {
        printlnf("Starting Scenario: %s", scenario.getName());
    }

    /**
     * Called at the beginning of an execution block, long before the steps are actually executed.
     */
    @Override
    public void step(Step step) {
    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {
    }

    @Override
    public void done() {
    }

    @Override
    public void close() {
    }

    @Override
    public void eof() {
    }

    /**
     * Called if there is a result during the setup hooks before the steps start
     */
    @Override
    public void before(Match match, Result result) {
        printlnf("Running @Before hook %s [%s]", match.getLocation(), result.getStatus());
        maybeRecordFailure(result);
    }

    /**
     * Called if there is a result during the tear down hooks after all the steps run
     */
    @Override
    public void after(Match match, Result result) {
        printlnf("Running @After hook %s [%s]", match.getLocation(), result.getStatus());
        maybeRecordFailure(result);
    }

    @Override
    public void match(Match match) {
        printlnf("Running %s", match.getLocation());
    }


    @Override
    public void result(Result result) {
        printlnf("Result: %s", result.getStatus());
        maybeRecordFailure(result);
    }

    private void maybeRecordFailure(Result result) {
        Throwable error = result.getError();
        if(error != null) {
            output.append(Throwables.getStackTraceAsString(error));
            passed = false;
        }
    }
    
    @Override
    public void embedding(String mimeType, byte[] data) {
        reporter.attach(mimeType, data);
    }

    @Override
    public void write(String text) {
        output.append(text);
    }
    
    private void printlnf(String format, Object... args) {
        output.append(String.format(format, args)).append("\n");
    }
    
    public Appendable asAppendable() {
        return output;
    }

    public String getOutput() {
        return output.toString();
    }

    public boolean isPassed() {
        return passed;
    }
}
