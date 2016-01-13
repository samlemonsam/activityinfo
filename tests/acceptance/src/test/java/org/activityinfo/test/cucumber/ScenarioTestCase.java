package org.activityinfo.test.cucumber;

import com.google.common.base.Throwables;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.Runtime;
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
import org.activityinfo.test.TestCase;
import org.activityinfo.test.TestConditions;
import org.activityinfo.test.TestResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Runs a single Cucumber scenario, or scenario outline
 */
public class ScenarioTestCase implements TestCase {

    private RuntimeOptions options;
    private final CucumberFeature feature;
    private final CucumberTagStatement featureElement;
    private final TestConditions testConditions;

    public ScenarioTestCase(RuntimeOptions options,
                            CucumberFeature feature,
                            CucumberTagStatement featureElement,
                            TestConditions testConditions) {
        this.options = options;
        this.feature = feature;
        this.featureElement = featureElement;
        this.testConditions = testConditions;
    }

    

    @Override
    public String getId() {
        return suiteNameFromPath() + "." + sanitizeScenarioName() + "." + testConditions.getId();
    }

    private String sanitizeScenarioName() {
        String name = featureElement.getGherkinModel().getName();
        return name.replace(".", "");
    }

    @Override
    public TestResult call() throws Exception {

        Runtime runtime = createCucumberRuntime();
    
        TestResult.Builder result = TestResult.builder(this);
        
        ReportingAdapter adapter = new ReportingAdapter(result);

        try {
            adapter.uri(feature.getPath());
            adapter.feature(feature.getGherkinFeature());

            featureElement.run(adapter, adapter, runtime);

            adapter.eof();
            adapter.done();
            adapter.close();

        } catch (Exception e) {
            
            result.failed();
            
            Throwable rootCause = Throwables.getRootCause(e);
            if(rootCause instanceof InterruptedException) {
                throw (InterruptedException)rootCause;
            }

            result.output().append("\n");
            result.output().append("Exception thrown by test runner: ").append(e.getMessage()).append("\n");
            result.output().append(Throwables.getStackTraceAsString(e));
        } finally {
            logRuntimeErrors(runtime, result);
        }
        return result.build();
    }

    private void logRuntimeErrors(Runtime runtime, TestResult.Builder result) {
        if (!runtime.getErrors().isEmpty()) {
            for (Throwable e : runtime.getErrors()) {
                result.output().append(Throwables.getStackTraceAsString(e));
            }
        }
    }

    private Runtime createCucumberRuntime() {
        List<Module> moduleList = new ArrayList<>();
        moduleList.add(new ScenarioModule(new SequentialScenarioScope()));
        moduleList.addAll(testConditions.getModules());

        Injector injector = Guice.createInjector(moduleList);

        ClassLoader classLoader = getClass().getClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        JavaBackend backend = new JavaBackend(new GuiceObjectFactory(injector), classFinder);
        return new Runtime(resourceLoader, classLoader, Collections.singleton(backend), options);
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
