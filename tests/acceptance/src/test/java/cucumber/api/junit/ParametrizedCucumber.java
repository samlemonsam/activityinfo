package cucumber.api.junit;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import cucumber.api.Profile;
import cucumber.runtime.*;
import cucumber.runtime.Runtime;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.java.JavaBackend;
import cucumber.runtime.java.ObjectFactory;
import cucumber.runtime.junit.*;
import cucumber.runtime.model.CucumberFeature;
import org.activityinfo.test.harness.ProfileFactoryImpl;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singleton;

/**
 * <p>
 * Classes annotated with {@code @RunWith(ParametrizedCucumber.class)} will run a Cucumber Feature against
 * a set of {@code ExecutionProfiles}.
 * 
 * The class should be empty without any fields or methods.
 * </p>
 * <p>
 * Cucumber will look for a {@code .feature} file on the classpath, using the same resource
 * path as the annotated class ({@code .class} substituted by {@code .feature}).
 * </p>
 * Additional hints can be given to Cucumber by annotating the class with {@link cucumber.api.CucumberOptions}.
 *
 * @see cucumber.api.CucumberOptions
 */
public class ParametrizedCucumber extends ParentRunner<ParametrizedFeatureRunner>  {
    private final ParametrizedJunitReporter jUnitReporter;
    
    private final List<ParametrizedFeatureRunner> children = new ArrayList<ParametrizedFeatureRunner>();
    private final List<ParametrizedRuntime> runtimes = Lists.newArrayList();

    /**
     * Constructor called by JUnit.
     *
     * @param clazz the class with the @RunWith annotation.
     * @throws java.io.IOException                         if there is a problem
     * @throws org.junit.runners.model.InitializationError if there is another problem
     */
    public ParametrizedCucumber(Class clazz) throws InitializationError, IOException {
        super(clazz);
        ClassLoader classLoader = clazz.getClassLoader();
        Assertions.assertNoCucumberAnnotatedMethods(clazz);

        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        ResourceLoader resourceLoader = new MultiLoader(classLoader);

        final List<CucumberFeature> cucumberFeatures = runtimeOptions.cucumberFeatures(resourceLoader);
        
        jUnitReporter = new ParametrizedJunitReporter(runtimeOptions.reporter(classLoader), 
                runtimeOptions.formatter(classLoader), runtimeOptions.isStrict());
        
        createParametrizedRuntimes(runtimeOptions, classLoader, resourceLoader);
        addChildren(cucumberFeatures);
    }

    @Override
    public List<ParametrizedFeatureRunner> getChildren() {
        return children;
    }

    @Override
    protected Description describeChild(ParametrizedFeatureRunner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(ParametrizedFeatureRunner child, RunNotifier notifier) {
        child.run(notifier);
    }

    @Override
    public void run(RunNotifier notifier) {
        super.run(notifier);
        jUnitReporter.done();
        jUnitReporter.close();
        //runtime.printSummary();
    }

    private void createParametrizedRuntimes(RuntimeOptions runtimeOptions, ClassLoader classLoader, ResourceLoader resourceLoader) throws InitializationError {


        ProfileFactory profileFactory = new ProfileFactoryImpl();
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        
        for (Profile profile : profileFactory.getProfiles()) {
            
            Optional<ObjectFactory> objectFactory = profileFactory.createObjectFactory(profile);
            if(objectFactory.isPresent()) {

                // Create a specific runtime for this execution profile

                JavaBackend backend = new JavaBackend(objectFactory.get(), classFinder);
                Runtime runtime = new Runtime(resourceLoader, classLoader, singleton(backend), runtimeOptions);
                ParametrizedRuntime parametrizedRuntime = new ParametrizedRuntime(profile, runtime);
                runtimes.add(parametrizedRuntime);

            }
        }
    }


    private void addChildren(List<CucumberFeature> cucumberFeatures) throws InitializationError {
        for (CucumberFeature cucumberFeature : cucumberFeatures) {
            children.add(new ParametrizedFeatureRunner(cucumberFeature, jUnitReporter, runtimes));
        }
    }
}
