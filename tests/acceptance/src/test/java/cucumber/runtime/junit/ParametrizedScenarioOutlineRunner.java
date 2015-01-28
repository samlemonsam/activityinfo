package cucumber.runtime.junit;

import cucumber.api.Profile;
import cucumber.runtime.model.CucumberScenarioOutline;
import org.junit.runner.Description;
import org.junit.runners.model.InitializationError;


public class ParametrizedScenarioOutlineRunner extends ScenarioOutlineRunner {

    private Profile profile;
    private Description description;

    public ParametrizedScenarioOutlineRunner(ParametrizedRuntime parametrizedRuntime, 
                                             CucumberScenarioOutline cucumberScenarioOutline, 
                                             JUnitReporter jUnitReporter) throws InitializationError {
        super(parametrizedRuntime.getRuntime(), cucumberScenarioOutline, jUnitReporter);
        this.profile = parametrizedRuntime.getProfile();
    }


    @Override
    public String getName() {
        return super.getName() + " [" + profile.getName() + "]";
    }

    @Override
    public Description getDescription() {
        if (description == null) {
            Description delegateDescription = super.getDescription();
            description = Description.createSuiteDescription(getName(),
                    new UniqueId(profile.getId(), delegateDescription));

            for (Description childDescription : delegateDescription.getChildren()) {
                description.addChild(childDescription);
            }
        }
        return description; 
    }
}
