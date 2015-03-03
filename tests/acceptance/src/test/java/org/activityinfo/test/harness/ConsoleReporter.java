package org.activityinfo.test.harness;


import com.google.common.collect.Lists;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;

import java.io.*;
import java.util.List;

/**
 * Simple test reporter for gradle executions
 */
public class ConsoleReporter implements Formatter, Reporter {


    private PrintStream out;
    private FileOutputStream log;


    private String currentScenario;
    private String currentFile;
    private int exampleCount;
    private int exampleNumber;
    private boolean failed;
    private boolean skipped;

    private List<String> failedScenarios = Lists.newArrayList();

    public ConsoleReporter() throws FileNotFoundException {
        out = System.out;

        // Send all the rest of the stuff to log files
        log = new FileOutputStream(System.getProperty("acceptanceTestLogFile", "acceptance-test.log"));
    //    System.setErr(new PrintStream(new LogOutputStream("STDERR: ")));
    //    System.setOut(new PrintStream(new LogOutputStream("STDOUT: ")));
    }

    @Override
    public void before(Match match, Result result) {
    }

    @Override
    public void result(Result result) {
        if(result.getStatus().equals(Result.FAILED)) {
            failed = true;
        } else if(result.getStatus().equals(Result.SKIPPED.getStatus())) {
            skipped = true;
        }
    }

    @Override
    public void after(Match match, Result result) {

    }

    @Override
    public void match(Match match) {

    }

    @Override
    public void embedding(String mimeType, byte[] data) {

    }

    @Override
    public void write(String text) {

    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {

    }

    @Override
    public void uri(String uri) {
        int slash = uri.lastIndexOf('/');
        if(slash == -1) {
            currentFile = uri;
        } else {
            currentFile = uri.substring(slash+1);
        }
    }

    @Override
    public void feature(Feature feature) {
        out.println(String.format("%s [%s]", feature.getName(), currentFile));
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        scenarioStarting(scenarioOutline.getName());
    }

    @Override
    public void scenario(Scenario scenario) {
        scenarioStarting(scenario.getName());
    }


    @Override
    public void examples(Examples examples) {
        this.exampleCount = examples.getRows().size();
    }

    private void scenarioStarting(String name) {
        this.currentScenario = name;
        this.failed = false;
        this.exampleCount = 1;
        this.exampleNumber = 1;
        failed = false;
        skipped = false;
    }

    @Override
    public void background(Background background) {

    }

    @Override
    public void step(Step step) {

    }

    @Override
    public void startOfScenarioLifeCycle(Scenario scenario) {
    }

    private void updateStatus(String status) {
        out.print("  ");
        out.print(currentScenario);
        if(exampleCount > 1) {
            out.printf(" (%d/%d): ", exampleNumber, exampleCount);
        } else {
            out.print(": ");
        }
        out.println(status);
    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {
        exampleNumber ++;
        if(failed) {
            updateStatus("FAILED");
        } else if(skipped) {
            updateStatus("SKIPPED");
        } else {
            updateStatus("OK");
        }
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

    private class LogOutputStream extends OutputStream {

        private String prefix;
        private ByteArrayOutputStream line = new ByteArrayOutputStream();

        public LogOutputStream(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public void write(int b) throws IOException {
            line.write(b);
            if(b == '\n') {
                if(log != null) {
                    log.write(prefix.getBytes());
                    log.write(line.toByteArray());
                    line.reset();
                }
            }
        }
    }
}
