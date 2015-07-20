package org.activityinfo.test;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.inject.Module;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.model.CucumberFeature;
import io.airlift.airline.Command;
import io.airlift.airline.HelpOption;
import io.airlift.airline.Option;
import io.airlift.airline.SingleCommand;
import org.activityinfo.test.cucumber.FeatureTestSuite;
import org.activityinfo.test.driver.ApiModule;
import org.activityinfo.test.driver.mail.EmailModule;
import org.activityinfo.test.sut.SystemUnderTest;
import org.activityinfo.test.webdriver.ChromeDriverModule;

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
    
    @Option(name = "--chrome", description = "Run tests using a local chrome browser")
    public boolean chrome;
    
    @Option(name = "--smoke", description = "Run smoke tests against the live production instance")
    public boolean smokeTests;

    @Option(name = "--outputDir", description = "Directory into which test result XML files are written")
    public File outputDir;
    
    @Option(name = "--filter", description = "Filters tests to run using a regular expression")
    public String filterRegex;

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
    
        
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
     
        if(api) {
            queueApiTests();
        }
        
        if(chrome) {
            queueWebTests(new ChromeDriverModule());
        }

        executor.shutdown();
        
        try {
            executor.awaitTermination(3, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            throw new RuntimeException("Test suite interrupted.");
        }
        
        executor.shutdownNow();
        
        stats.printSummary();
        
        if(stats.hasFailures()) {
            System.exit(-1);
        } else {
            System.exit(0);
        }
    }

    private void queueApiTests() {
        ResourceLoader loader = new MultiLoader(getClass().getClassLoader());
        ClassFinder classFinder = new ResourceLoaderClassFinder(loader, getClass().getClassLoader());
        RuntimeOptions options = new RuntimeOptions(Arrays.asList(
                "--tags", "@api", "classpath:org/activityinfo/test",
                "--glue", "org.activityinfo.test.steps.common", 
                "--glue", "org.activityinfo.test.steps.json"));


        queueFeatures(loader, options, new ApiModule());
    }

    private void queueWebTests(Module driverModule) {
        ResourceLoader loader = new MultiLoader(getClass().getClassLoader());
        ClassFinder classFinder = new ResourceLoaderClassFinder(loader, getClass().getClassLoader());
        RuntimeOptions options = new RuntimeOptions(Arrays.asList(
                "--tags", "@web", "classpath:org/activityinfo/test",
                "--glue", "org.activityinfo.test.steps.common",
                "--glue", "org.activityinfo.test.steps.web"));


        queueFeatures(loader, options, driverModule);
    }

    private void queueFeatures(ResourceLoader loader, RuntimeOptions options, Module... driverModules) {
        
        List<Module> modules = new ArrayList<>();
        modules.add(new SystemUnderTest(url));
        modules.add(new EmailModule());
        modules.addAll(Arrays.asList(driverModules));
        
        List<CucumberFeature> features = options.cucumberFeatures(loader);
        for (CucumberFeature feature : features) {
            CiTestReporter reporter = new CiTestReporter(outputDir, stats);
            
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
