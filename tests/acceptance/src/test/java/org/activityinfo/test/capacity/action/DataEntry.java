package org.activityinfo.test.capacity.action;

import org.activityinfo.test.driver.ApiApplicationDriver;

import java.util.List;


public class DataEntry implements UserAction {
    @Override
    public void execute(ApiApplicationDriver driver) throws Exception {

        // Choose a database for which to perform data entry
        List<String> databases = driver.getEditableDatabases();
        
        if(databases.isEmpty()) {
            return;
        }
        
        String database = Sampling.chooseOne(databases);

        List<String> forms = driver.getForms(database);
        if(forms.isEmpty()) {
            
        }


    }
}
