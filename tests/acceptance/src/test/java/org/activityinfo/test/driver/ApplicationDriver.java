package org.activityinfo.test.driver;


import com.google.inject.ImplementedBy;
import cucumber.api.DataTable;
import cucumber.api.Pending;
import cucumber.api.PendingException;
import org.activityinfo.test.sut.UserAccount;
import org.json.JSONException;

import java.util.List;

public abstract class ApplicationDriver {
    
    private final AliasTable aliasTable;

    public ApplicationDriver(AliasTable aliasTable) {
        this.aliasTable = aliasTable;
    }

    /**
     * Login as any user
     */
    public abstract void login();
    
    public abstract void login(UserAccount account);

    /**
     * @return an implementation of ApplicationDriver suitable for setting up a test scenario
     */
    public ApplicationDriver setup() {
        return this;
    }
    
    public final void createDatabase(Property... properties) throws Exception {
        createDatabase(new TestObject(aliasTable, properties));
    }

    protected void createDatabase(TestObject database) throws Exception {
        throw new PendingException();
    }

    public final void createForm(Property... properties) throws Exception {
        createForm(new TestObject(aliasTable, properties));
    }

    protected void createForm(TestObject form) throws Exception {
        throw new PendingException();
    }

    public void createField(Property... properties) throws Exception {
        createField(new TestObject(aliasTable, properties));
    }

    public void createField(TestObject testObject) throws Exception {
        throw new PendingException();
    }

    public void submitForm(String formName, List<FieldValue> values) throws Exception {
        throw new PendingException();
    }
    
    public void delete(String objectType, String name) throws Exception {
        throw new PendingException();
    }

    public void addPartner(String partnerName, String databaseName) throws  Exception {
        throw new PendingException();
    }

    public void createTarget(Property... properties) throws Exception {
        createTarget(new TestObject(aliasTable, properties));
    }

    protected void createTarget(TestObject target) throws Exception {
        throw new PendingException();
    }

    public void setTargetValues(String targetName, List<FieldValue> values) throws Exception {
        throw new PendingException();
    }

    public final void createProject(Property... properties) throws Exception {
        createProject(new TestObject(aliasTable, properties));
    }

    protected void createProject(TestObject project) throws Exception {
        throw new PendingException();
    }

    public DataTable pivotTable(String measure, List<String> rowDimensions) throws Exception {
        throw new PendingException();
    }

    public final void grantPermission(Property... properties) throws Exception {
        grantPermission(new TestObject(aliasTable, properties));
    }
    

    protected void grantPermission(TestObject permission) throws Exception {
        throw new PendingException();
    }

    public void cleanup() throws Exception {
        
    }

    public final void createLocationType(Property... properties) throws Exception {
        createLocationType(new TestObject(aliasTable, properties));
    }

    protected void createLocationType(TestObject testObject) throws Exception {
        throw new PendingException();
    }

    public final void createLocation(Property... properties) throws Exception {
        createLocation(new TestObject(aliasTable, properties));
        
    }

    protected void createLocation(TestObject testObject) throws Exception {
        throw new PendingException();
    }
    
    public abstract ApplicationDriver withNamespace(AliasTable aliasTable);
}
