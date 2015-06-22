package org.activityinfo.test;

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
import org.activityinfo.test.sut.SystemUnderTest;

import javax.inject.Inject;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Command line tool for running the acceptance test suite
 */
@Command(name = "test")
public class TestMain implements Runnable {

    @Inject
    public HelpOption helpOption;
    
    @Option(name = "--url", description = "The URL of the deployment to test")
    public String url = "http://localhost:8888";
    
    @Option(name = "--api", description = "Run functional tests against the API")
    public boolean api;
    
    @Option(name = "--chrome", description = "Run tests using a local chrome browser")
    public boolean chrome;
    
    @Option(name = "--smoke", description = "Run smoke tests against the live production instance")
    public boolean smokeTests;

    @Option(name = "--outputDir", description = "Directory into which test result XML files are written")
    public File outputDir;

    private TestStats stats = new TestStats();
    private ExecutorService executor;

    public static void main(String[] args) {

        TestMain suite = SingleCommand.singleCommand(TestMain.class).parse(args);

        if (suite.helpOption.showHelpIfRequested()) {
            return;
        }
        
        suite.run();
    }


    public void run() {
        
        if(outputDir != null) {
            boolean created = outputDir.mkdirs();
            if(!created) {
                throw new RuntimeException("Failed to create " + outputDir.getAbsolutePath());
            }
        }
        
        executor = Executors.newFixedThreadPool(1);
     
        if(api) {
            queueApiTests(executor);
        }

        executor.shutdown();
        
        try {
            executor.awaitTermination(3, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            throw new RuntimeException("Test suite interrupted.");
        }
        
        executor.shutdownNow();
        
        stats.printSummary();
    }
    
    private void queueApiTests(ExecutorService executor) {
        ResourceLoader loader = new MultiLoader(getClass().getClassLoader());
        ClassFinder classFinder = new ResourceLoaderClassFinder(loader, getClass().getClassLoader());
        RuntimeOptions options = new RuntimeOptions(Arrays.asList(
                "--tags", "@api", "classpath:org/activityinfo/test",
                "--glue", "org.activityinfo.test.steps.common", 
                "--glue", "org.activityinfo.test.steps.json"));
        
        
        List<CucumberFeature> features = options.cucumberFeatures(loader);
        for (CucumberFeature feature : features) {
            Future<?> result = executor.submit(new FeatureTestSuite(options, feature, new CiTestReporter(outputDir, stats),
                    new ApiModule(), new SystemUnderTest(url)));
            try {
                result.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
}
