package org.activityinfo.test.capacity.scripts;

import org.activityinfo.test.capacity.model.Scenario;

import java.util.Collection;
import java.util.Collections;

public class NullScript implements CapacityTestScript {
    @Override
    public Collection<Scenario> get() {
        return Collections.emptyList();
    }
}
