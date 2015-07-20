package org.activityinfo.test.cucumber;

import com.google.api.client.repackaged.com.google.common.base.Throwables;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.java.JavaBackend;
import cucumber.runtime.java.ObjectFactory;
import cucumber.runtime.java.guice.ScenarioScope;
import cucumber.runtime.java.guice.impl.ScenarioModule;
import cucumber.runtime.java.guice.impl.SequentialScenarioScope;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberTagStatement;
import org.activityinfo.test.TestLogger;
import org.activityinfo.test.TestReporter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Runs a single Cucumber .feature file. Each Scenario or Scenario outline is 
 * considered a "test
 */
public class FeatureTestSuite implements Runnable {

    private final Injector injector;
    private final CucumberFeature feature;
    private final RuntimeOptions options;
    private final TestReporter reporter;
    private final cucumber.runtime.Runtime runtime;
    private ReportingAdapter adapter;

    public FeatureTestSuite(RuntimeOptions options, CucumberFeature feature, TestReporter reporter, List<Module> modules) {
        this.feature = feature;
        this.options = options;
        this.reporter = reporter;

        List<Module> moduleList = new ArrayList<>();
        moduleList.add(new ScenarioModule(new SequentialScenarioScope()));
        moduleList.addAll(modules);
        moduleList.add(new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(TestLogger.class).toInstance(new TestLogger() {

                    @Override
                    public void info(String message) {
                        if (adapter != null) {
                            adapter.write(message);
                        }
                    }
                });
            }
        });

        this.injector = Guice.createInjector(moduleList);

        ClassLoader classLoader = getClass().getClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        JavaBackend backend = new JavaBackend(new GuiceObjectFactory(injector), classFinder);
        runtime = new cucumber.runtime.Runtime(resourceLoader, classLoader, Collections.singleton(backend), options);
    }

    @Override
    public void run() {

        reporter.testSuiteStarted(suiteNameFromPath());

        for (CucumberTagStatement element : feature.getFeatureElements()) {
            try {
                runScenario(element);
            } catch (InterruptedException e) {
                break;
            }
        }

        reporter.testSuiteFinished();
    }

    private String suiteNameFromPath() {
        String path = feature.getPath().replace('\\', '/');
        int nameBegin = path.lastIndexOf('/');
        int extensionBegin = path.lastIndexOf('.');
        if(extensionBegin == -1) {
            extensionBegin = path.length();
        }
        return path.substring(nameBegin+1, extensionBegin);
    }

    private void runScenario(CucumberTagStatement element) throws InterruptedException {

        // Check to see if we've been aborted, 
        if(Thread.interrupted()) {
            return;
        }
        
        String testName = element.getGherkinModel().getName();
        adapter = new ReportingAdapter();
        reporter.testStarted(testName);
        
        try {
            adapter.uri(feature.getPath());
            adapter.feature(feature.getGherkinFeature());

            element.run(adapter, adapter, runtime);

            adapter.eof();
            adapter.done();
            adapter.close();
            reporter.testFinished(testName, adapter.isPassed(), adapter.getOutput());
            
            
        } catch (Exception e) {
            Throwable rootCause = Throwables.getRootCause(e);
            if(rootCause instanceof InterruptedException) {
                throw (InterruptedException)rootCause;
            }
            
            StringBuilder output = new StringBuilder(adapter.getOutput());
            output.append("\n");
            output.append("Exception thrown by test runner: ").append(e.getMessage()).append("\n");
            output.append(Throwables.getStackTraceAsString(e));
            reporter.testFinished(testName, false, output.toString());
        }


    }

    private static class GuiceObjectFactory implements ObjectFactory {

        private final Injector injector;

        private GuiceObjectFactory(Injector injector) {
            this.injector = injector;
        }

        @Override
        public void addClass(Class<?> clazz) {}

        @Override
        public void start() {
            injector.getInstance(ScenarioScope.class).enterScope();
        }

        @Override
        public void stop() {
            injector.getInstance(ScenarioScope.class).exitScope();
        }

        @Override
        public <T> T getInstance(Class<T> clazz) {
            return injector.getInstance(clazz);
        }

    }
}
