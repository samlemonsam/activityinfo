package org.activityinfo.test.capacity.scenario.cm;

import org.activityinfo.store.testing.IntakeForm;
import org.activityinfo.test.api.Cuids;
import org.activityinfo.test.api.TestDatabase;
import org.activityinfo.test.capacity.action.UserAction;
import org.activityinfo.test.driver.ApplicationDriver;

public class DatabaseSetup implements UserAction {
    @Override
    public void execute(ApplicationDriver driver) throws Exception {


        TestDatabase database = driver.createDatabase("Registration");
        IntakeForm intakeForm = new IntakeForm(new Cuids(database));

        driver.getClient().createForm(intakeForm.getFormClass());



    }
}
