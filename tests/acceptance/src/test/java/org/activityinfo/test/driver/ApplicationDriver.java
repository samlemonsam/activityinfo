package org.activityinfo.test.driver;


import com.google.inject.ImplementedBy;
import cucumber.api.DataTable;
import org.activityinfo.test.sut.UserAccount;
import org.json.JSONException;

import java.util.List;

public interface ApplicationDriver {

    
    /**
     * Login as any user
     */
    void login();
    
    void login(UserAccount account);

    /**
     * @return an implementation of ApplicationDriver suitable for setting up a test scenario
     */
    ApplicationDriver setup();
    
    void createDatabase(Property... properties) throws Exception;

    void createForm(Property... properties) throws Exception;

    void createField(Property... properties) throws Exception;

    void submitForm(String formName, List<FieldValue> values) throws Exception;
    
    void delete(String objectType, String name) throws Exception;

    void addPartner(String partnerName, String databaseName) throws  Exception;

    void createTarget(Property... properties) throws Exception;
    
    void setTargetValues(String targetName, List<FieldValue> values) throws Exception;

    void createProject(Property... properties) throws Exception;

    DataTable pivotTable(String measure, List<String> rowDimensions);

    void grantPermission(Property... properties) throws Exception;

    void cleanup() throws Exception;

}
