package org.activityinfo.test;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.inject.Module;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import io.airlift.airline.Command;
import io.airlift.airline.HelpOption;
import io.airlift.airline.Option;
import io.airlift.airline.SingleCommand;
import org.activityinfo.test.cucumber.FeatureTestSuite;
import org.activityinfo.test.driver.ApiModule;
import org.activityinfo.test.driver.mail.EmailModule;
import org.activityinfo.test.sut.SystemUnderTest;
import org.activityinfo.test.webdriver.OdkModule;
import org.activityinfo.test.webdriver.WebDriverModule;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

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
    
    @Option(name = "-T", description = "Number of threads to use")
    public int threadCount = Runtime.getRuntime().availableProcessors();
    

    private TestStats stats = new TestStats();
    private ExecutorService executor;

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
        
        executor.shutdown();
        
        try {
            executor.awaitTermination(3, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            throw new RuntimeException("Test suite interrupted.");
        }
        
        executor.shutdownNow();
        stats.finished();
        stats.printSummary();
        
        if(stats.hasFailures()) {
            System.exit(-1);
        } else {
            System.exit(0);
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
        
        List<CucumberFeature> features = options.cucumberFeatures(loader);
        for (CucumberFeature feature : features) {
            CiTestReporter reporter = new CiTestReporter(environment, outputDir, stats);
            
            executor.submit(new FeatureTestSuite(options, feature, reporter, filterPredicate(), modules));
        }
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
