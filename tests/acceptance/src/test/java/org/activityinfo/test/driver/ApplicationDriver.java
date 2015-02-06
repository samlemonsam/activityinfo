package org.activityinfo.test.driver;


import com.google.inject.ImplementedBy;
import org.json.JSONException;

import java.util.List;

@ImplementedBy(ApiApplicationDriver.class)
public interface ApplicationDriver {

    
    /**
     * Login as any user
     */
    void login();
    
    void createDatabase(Property... properties) throws Exception;

    void createForm(Property... properties) throws Exception;

    void createField(Property... properties) throws Exception;

    void submitForm(String formName, List<FieldValue> values) throws Exception;

    void addPartner(String partnerName, String databaseName) throws  Exception;

    void createTarget(Property... properties) throws JSONException, Exception;
    
    void setTargetValues(String targetName, List<FieldValue> values);
}
