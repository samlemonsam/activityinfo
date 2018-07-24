package org.activityinfo.test.capacity.scenario.cm;

import org.activityinfo.test.capacity.model.Scenario;
import org.activityinfo.test.capacity.model.UserRole;

import java.util.ArrayList;
import java.util.List;

public class CaseManagementScenario implements Scenario {

    private int caseWorkerCount = 30;

    public CaseManagementScenario(int caseWorkerCount) {
        this.caseWorkerCount = caseWorkerCount;
    }

    @Override
    public List<UserRole> getUsers() {
        List<UserRole> roles = new ArrayList<>();
        for (int i = 0; i < caseWorkerCount; i++) {
            roles.add(new CaseWorker());
        }
        return roles;
    }

    @Override
    public int getDayCount() {
        throw new UnsupportedOperationException("TODO");
    }
}
