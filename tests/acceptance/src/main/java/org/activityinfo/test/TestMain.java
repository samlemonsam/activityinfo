package org.activityinfo.test;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.util.concurrent.Futures;
import com.google.inject.Module;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberTagStatement;
import io.airlift.airline.Command;
import io.airlift.airline.HelpOption;
import io.airlift.airline.Option;
import io.airlift.airline.SingleCommand;
import org.activityinfo.test.cucumber.ScenarioTestCase;
import org.activityinfo.test.driver.ApiModule;
import org.activityinfo.test.driver.mail.EmailModule;
import org.activityinfo.test.sut.SystemUnderTest;
import org.activityinfo.test.webdriver.OdkModule;
import org.activityinfo.test.webdriver.WebDriverModule;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static java.lang.String.format;

/**
 * Command line tool for running the acceptance test suite
 */
@Command(name = "test")
public class TestMain implements Runnable {

    @Inject
    public HelpOption helpOption;

    @Option(name = "--url", description = "The URL of the deployment to test")
    public String url = "http://localhost:8080";

    @Option(name = "--api", description = "Run functional tests against the API")
    public boolean api;

    @Option(name = "--ui", description = "Run functional tests against the UI")
    public boolean ui;

    @Option(name = "--odk", description = "Run ODK integration tests")
    public boolean odk;

    @Option(name = "--webdriver",
            title = "chrome | phantomjs | sauce",
            description = "Select the WebDriver to use for executing tests against the UI: chrome, phantomjs or sauce.",
            allowedValues = { "chrome", "phantomjs", "sauce" }
    )
    public String webDriverType;

    @Option(name = "--smoke", description = "Run smoke tests against the live production instance")
    public boolean smokeTests;

    @Option(name = "--outputDir", description = "Directory into which test result XML files are written")
    public File outputDir;

    @Option(name = "--filter", description = "Filters tests to run using a regular expression")
    public String filterRegex;

    @Option(name = "--ignore-failures", description = "Always exit with success, even if there are test failures")
    public boolean ignoreFailures = false;
    
    @Option(name = "--retries", description = "Number of times to retry failed tests")
    public int retries;

    @Option(name = "-T", description = "Number of threads to use")
    public int threadCount = Runtime.getRuntime().availableProcessors();

    private RetryCounter retryCounter = new RetryCounter();
    private TestStats stats = new TestStats();
    private ExecutorService executor;
    private List<Future<TestResult>> pending = Lists.newArrayList();
 
    
    public static void main(String[] args) {

        TestOutputStream.initialize();

        TestMain suite = SingleCommand.singleCommand(TestMain.class).parse(args);

        if (suite.helpOption.showHelpIfRequested()) {
            return;
        }

        suite.run();
    }
    
    public void run() {

        if(outputDir == null) {
            outputDir = new File("build/test-reports");
        }
        if(!outputDir.exists()) {
            boolean created = outputDir.mkdirs();
            if (!created) {
                throw new RuntimeException("Failed to create " + outputDir.getAbsolutePath());
            }
        }


        executor = Executors.newFixedThreadPool(threadCount);

        if(api) {
            queueApiTests();
        }

        if(ui) {
            queueUiTests();
        }

        if(odk) {
            queueOdkTests();
        }
        
        System.out.println(format("Queued %d test(s)", pending.size()));
        
        while(!pending.isEmpty()) {
            Iterator<Future<TestResult>> it = pending.iterator();
            List<TestResult> completed = Lists.newArrayList();
            while(it.hasNext()) {
                Future<TestResult> result = it.next();
                if(result.isDone()) {
                    completed.add(Futures.getUnchecked(result));
                    it.remove();
                }
            }
            for (TestResult testResult : completed) {
                onTestComplete(testResult);
            }
        }
        
        TestReportWriter writer = new TestReportWriter(outputDir);
        writer.write(stats.getResults());
        
        executor.shutdown();

        try {
            executor.awaitTermination(3, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            throw new RuntimeException("Test suite interrupted.");
        }
        
        executor.shutdownNow();
        stats.finished();
        stats.printSummary();

        if(ignoreFailures) {
            System.exit(0);
        }

        if(stats.hasFailures()) {
            System.exit(-1);
        } else {
            System.exit(0);
        }
    }

    private void onTestComplete(TestResult result) {
        stats.recordTime(result);
        printTestResult(result);
        writeTestLog(result);
        
        if(result.isPassed() || retryCounter.getAttemptNumber(result) >= retries) {
            stats.recordResult(result);
        } else {
            // Retry, so don't record the final result yet
            queueTestCase(result.getTestCase());
        }
    }

    /**
     * Write the test log immediately when the test completes
     */
    private void writeTestLog(TestResult result) {
        try {
            String logFileName = result.getId() + ".log";
            int attemptIndex = retryCounter.getAttemptNumber(result);
            if(attemptIndex > 0) {
                logFileName += "." + attemptIndex;
            }
            File logFile = new File(outputDir, logFileName);
            Files.write(result.getOutput(), logFile, Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Exception writing output log", e);
        }
    }

    private void printTestResult(TestResult result) {
        TestOutputStream.getStandardOutput().println(format("%5s %s (%.1f s) %s",
                (result.isPassed() ? "OK" : "FAIL"), 
                result.getId(), 
                result.getDuration() / 1000d,
                retryLabel(result)));
    }

    private String retryLabel(TestResult result) {
        int attemptIndex = retryCounter.getAttemptNumber(result);
        if(attemptIndex == 0) {
            return "";
        } else {
            return " RETRY #" + attemptIndex;
        }
    }

    private void queueApiTests() {
        ResourceLoader loader = new MultiLoader(getClass().getClassLoader());
        RuntimeOptions options = new RuntimeOptions(Arrays.asList(
                "--tags", "@api", "classpath:org/activityinfo/test",
                "--glue", "org.activityinfo.test.steps.common",
                "--glue", "org.activityinfo.test.steps.json"));


        queueFeatures("api", loader, options, new ApiModule());
    }

    private void queueUiTests() {
        ResourceLoader loader = new MultiLoader(getClass().getClassLoader());
        RuntimeOptions options = new RuntimeOptions(Arrays.asList(
                "--tags", "@web", "classpath:org/activityinfo/test",
                "--glue", "org.activityinfo.test.steps.common",
                "--glue", "org.activityinfo.test.steps.web"));


        queueFeatures("ui", loader, options, new WebDriverModule(webDriverType));
        queueTestMethods("ui", new WebDriverModule(webDriverType));
    }

    /**
     * Some extra checks with the browser set to different timezones
     */
    private void queueTimezoneTests() {
        
    }
    
    private void queueOdkTests() {
        ResourceLoader loader = new MultiLoader(getClass().getClassLoader());
        RuntimeOptions options = new RuntimeOptions(Arrays.asList(
                "--tags", "@odk", "classpath:org/activityinfo/test",
                "--glue", "org.activityinfo.test.steps.common",
                "--glue", "org.activityinfo.test.steps.odk"));


        queueFeatures("odk", loader, options, new OdkModule());
    }


    private void queueFeatures(String environment, ResourceLoader loader, RuntimeOptions options,
                               Module... driverModules) {

        
        List<Module> modules = new ArrayList<>();
        modules.add(new SystemUnderTest(url));
        modules.add(new EmailModule());
        modules.addAll(Arrays.asList(driverModules));

        TestConditions conditions = new TestConditions(environment, modules);

        Predicate<String> filter = filterPredicate();

        List<CucumberFeature> features = options.cucumberFeatures(loader);
        for (CucumberFeature feature : features) {
            for (CucumberTagStatement element : feature.getFeatureElements()) {
                if(filter.apply(feature.getPath()) || filter.apply(element.getVisualName())) {
                    ScenarioTestCase testCase = new ScenarioTestCase(options, feature, element, conditions);
                    queueTestCase(testCase);
                }
            }
        }
    }
    
    private void queueTestMethods(String environment, Module... driverModules) {

        List<Module> modules = new ArrayList<>();
        modules.add(new SystemUnderTest(url));
        modules.add(new EmailModule());
        modules.addAll(Arrays.asList(driverModules));
        
        TestConditions conditions = new TestConditions(environment, modules);

        Predicate<String> filter = filterPredicate();

        ClassLoader classLoader = getClass().getClassLoader();
        ClassFinder classFinder = new ResourceLoaderClassFinder(new MultiLoader(classLoader), classLoader);
        Collection<Class<?>> testClasses = classFinder.getDescendants(Object.class, "org.activityinfo.test.ui");

        for (Class<?> testClass : testClasses) {
            for (Method method : JUnitUiTestCase.findTestMethods(testClass)) {
                if (filter.apply(method.getName()) || filter.apply(method.getDeclaringClass().getName())) {
                    queueTestCase(new JUnitUiTestCase(method, conditions));
                }
            }
        }
    }

    private void queueTestCase(TestCase testCase) {
        pending.add(executor.submit(testCase));
    }

    private Predicate<String> filterPredicate() {
        if(Strings.isNullOrEmpty(filterRegex)) {
            return Predicates.alwaysTrue();
        } else {
            final Pattern pattern = Pattern.compile(filterRegex);
            return new Predicate<String>() {
                @Override
                public boolean apply(String input) {
                    return pattern.matcher(input).find();
                }
            };
        }
    }

}
