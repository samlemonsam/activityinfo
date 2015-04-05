package org.activityinfo.test.capacity.scripts;

import org.activityinfo.test.capacity.model.Scenario;
import org.activityinfo.test.capacity.scenario.coordination.CoordinationScenario;

import java.util.Arrays;
import java.util.Collection;

/**
 * Runs scenarios which specifically stress on the number of form submissions
 */
public class SiteStress implements CapacityTestScript {
    @Override
    public Collection<Scenario> get() {
        return Arrays.<Scenario>asList(new CoordinationScenario(1, 10, 10));
    }
}
