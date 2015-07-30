package org.activityinfo.test.cucumber;

import com.google.common.base.Throwables;
import com.google.common.io.ByteSource;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;
import org.activityinfo.test.TestResult;

import java.util.List;

public class ReportingAdapter implements Formatter, Reporter {

    private TestResult.Builder resultBuilder;
    
    private int attachmentIndex = 1;

    public ReportingAdapter(TestResult.Builder result) {
        resultBuilder = result;
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
        printlnf("Running Background:");
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
            resultBuilder.output().append(Throwables.getStackTraceAsString(error));
            resultBuilder.failed();
        }
    }
    
    @Override
    public void embedding(String mimeType, byte[] data) {
        resultBuilder.attach(nextFilename(mimeType), ByteSource.wrap(data));
    }

    private String nextFilename(String mimeType) {
        if(mimeType.equals("image/png")) {
            return "image" + (attachmentIndex++) + ".png";
        } else if(mimeType.equals("application/vnd.ms-excel")) {
            return "excel" + (attachmentIndex++) + ".xls";
        } else {
            return "attachment" + (attachmentIndex++);
        }
    }

    @Override
    public void write(String text) {
        resultBuilder.output().append(text);
    }
    
    private void printlnf(String format, Object... args) {
        resultBuilder.output().append(String.format(format, args)).append("\n");
    }

}
