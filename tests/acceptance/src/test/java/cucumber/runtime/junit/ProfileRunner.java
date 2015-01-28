package cucumber.runtime.junit;

import cucumber.api.Profile;
import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberFeature;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs a single profile
 */
public class ProfileRunner extends ParentRunner<FeatureRunner> {

    private Profile profile;
    private final Runtime runtime;
    private JUnitReporter jUnitReporter;

    private List<FeatureRunner> children = new ArrayList<>();
    private Description description;


    public ProfileRunner(Profile profile, List<CucumberFeature> features,
                            Runtime runtime,
                            JUnitReporter jUnitReporter) throws InitializationError {
        super(null);
        this.profile = profile;
        this.runtime = runtime;
        this.jUnitReporter = jUnitReporter;
        
        addChildren(features);
    }


    @Override
    protected List<FeatureRunner> getChildren() {
        return children;
    }

    @Override
    protected String getName() {
        return profile.getName();
    }

    @Override
    protected Description describeChild(FeatureRunner child) {
        return child.getDescription();
    }

    @Override
    public Description getDescription() {
        if (description == null) {
            description = Description.createSuiteDescription(getName(), profile.getId());
            for (FeatureRunner child : getChildren()) {
                description.addChild(describeChild(child));
            }
        }
        return description;
    }

    @Override
    public void run(RunNotifier notifier) {
        super.run(notifier);
       // runtime.printSummary();
    }

    @Override
    protected void runChild(FeatureRunner child, RunNotifier notifier) {
        child.run(notifier);
    }
    
    private void addChildren(List<CucumberFeature> cucumberFeatures) throws InitializationError {
        for (CucumberFeature cucumberFeature : cucumberFeatures) {
            children.add(new FeatureRunner(cucumberFeature, runtime, jUnitReporter));
        }
    }
}
