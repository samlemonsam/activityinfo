package org.activityinfo.test.capacity.scenario.cm;

import org.activityinfo.test.capacity.action.UserAction;
import org.activityinfo.test.capacity.model.UserRole;

import java.util.List;

public class DbAdmin implements UserRole {

    @Override
    public String getNickName() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public List<UserAction> getTask(int dayNumber) {
        throw new UnsupportedOperationException("TODO");
    }
}
