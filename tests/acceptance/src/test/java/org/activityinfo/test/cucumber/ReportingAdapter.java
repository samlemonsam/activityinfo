package org.activityinfo.test.cucumber;

import com.google.common.base.Throwables;
import gherkin.formatter.Formatter;
import gherkin.formatter.PrettyFormatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;

import java.util.ArrayList;
import java.util.List;

import static cucumber.runtime.Runtime.isPending;

public class ReportingAdapter implements Formatter, Reporter {

    private StringBuilder output;
    private boolean passed = true;
    private PrettyFormatter formatter;
    
    private List<Throwable> errors = new ArrayList<>();

    public ReportingAdapter() {
        output = new StringBuilder();
        formatter = new PrettyFormatter(output, true, true);
    }

    @Override
    public void feature(Feature feature) {
        formatter.feature(feature);
    }

    @Override
    public void uri(String uri) {
        formatter.uri(uri);
    }
    
    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
    }


    @Override
    public void background(Background background) {
        output.append("Background:\n");
    }
    
    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        formatter.scenarioOutline(scenarioOutline);
    }

    @Override
    public void examples(Examples examples) {
        formatter.examples(examples);
    }

    @Override
    public void startOfScenarioLifeCycle(Scenario scenario) {
    }

    @Override
    public void scenario(Scenario scenario) {
        formatter.scenario(scenario);
    }

    @Override
    public void step(Step step) {
        formatter.step(step);
    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {
    }

    @Override
    public void done() {
        formatter.done();
    }

    @Override
    public void close() {
        formatter.close();
    }

    @Override
    public void eof() {
        formatter.eof();
    }

    @Override
    public void before(Match match, Result result) {
        formatter.before(match, result);
    }

    @Override
    public void result(Result result) {
        formatter.result(result);
        
        Throwable error = result.getError();
        if (isPendingOrUndefined(result)) {
            passed = false;

        } else if(error != null) {
            output.append(Throwables.getStackTraceAsString(error));
            passed = false;
        }
    }

    private boolean isPendingOrUndefined(Result result) {
        Throwable error = result.getError();
        return Result.UNDEFINED == result || isPending(error);
    }

    @Override
    public void after(Match match, Result result) {
        formatter.result(result);
    }

    @Override
    public void match(Match match) {
        formatter.match(match);
    }

    @Override
    public void embedding(String mimeType, byte[] data) {
        formatter.embedding(mimeType, data);
    }

    @Override
    public void write(String text) {
        formatter.write(text);
    }

    public String getOutput() {
        return output.toString();
    }

    public boolean isPassed() {
        return passed;
    }
}
