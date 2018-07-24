package org.activityinfo.test.capacity;

import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.store.testing.IntakeForm;
import org.activityinfo.store.testing.RecordGenerator;
import org.activityinfo.test.api.Cuids;
import org.activityinfo.test.api.TestDatabase;
import org.activityinfo.test.driver.AliasTable;
import org.activityinfo.test.driver.ApplicationDriver;
import org.activityinfo.test.sut.DevServerAccounts;
import org.activityinfo.test.sut.Server;
import org.activityinfo.test.sut.UserAccount;

public class CaseManagement {

    private final ApplicationDriver driver;
    private final IntakeForm form;
    private final RecordGenerator generator;

    public static void main(String[] args) {
        new CaseManagement().run();
    }



    public CaseManagement() {
        Server server = new Server("https://columns-dot-ai-dev.appspot.com");
        DevServerAccounts accounts = new DevServerAccounts(server);
        UserAccount adminUser = accounts.any();
        AliasTable aliasTable = new AliasTable();
        driver = new ApplicationDriver(server, aliasTable, adminUser);
        TestDatabase database = driver.createDatabase();
        form = new IntakeForm(new Cuids(database));

        driver.getClient().createForm(form.getFormClass());

        generator = form.getGenerator();

        QueryModel queryModel = form.queryAll();

        for (int i = 0; i < 10; i++) {
            driver.getClient().createRecord(generator.get());
            System.out.println("Added record");
            ColumnSet columnSet = driver.getClient().queryTable(queryModel);
            System.out.println("Queried " + columnSet.getNumRows() + " rows");
        }
    }

    private void run() {

        for (int i = 0; i < 10; i++) {
            driver.getClient().createRecord(generator.get());



        }
    }

    private void queryColumnSet() {
        QueryModel queryModel = new QueryModel();

    }

}
