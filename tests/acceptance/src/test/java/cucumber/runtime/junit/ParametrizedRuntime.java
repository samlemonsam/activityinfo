package cucumber.runtime.junit;

import cucumber.api.Profile;
import cucumber.runtime.*;
import cucumber.runtime.Runtime;


public class ParametrizedRuntime {
    private Profile profile;
    private cucumber.runtime.Runtime runtime;

    public ParametrizedRuntime(Profile profile, Runtime runtime) {
        this.profile = profile;
        this.runtime = runtime;
    }

    public Runtime getRuntime() {
        return runtime;
    }

    public Profile getProfile() {
        return profile;
    }

    public String decorateName(String visualName) {
        return String.format("%s [%s]", visualName, profile.getName());
    }
}
