package org.activityinfo.server.command.handler;

import com.google.inject.Inject;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.fixtures.Modules;
import org.activityinfo.fixtures.TestHibernateModule;
import org.activityinfo.model.permission.Operation;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.permission.Permission;
import org.activityinfo.model.permission.PermissionOracle;
import org.activityinfo.model.permission.PermissionQuery;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.database.DatabaseModule;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.store.spi.DatabaseProvider;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

@RunWith(InjectionSupport.class)
@Modules({
        TestHibernateModule.class,
        DatabaseModule.class
})
@OnDataSet("/dbunit/sites-simple2.db.xml")
public class PermissionTest {

    @Inject
    protected DatabaseProvider provider;

    private static final int DB_ID = 2;
    private static final ResourceId FORM_ID = CuidAdapter.activityFormClass(3);

    private static final int OWNER_ID = 1;
    // AUTH_USER has all permissions other than design
    private static final int AUTH_USER_ID = 2;
    // AUTH_RESTRICTED_USER has only view and edit permissions on partner
    private static final int AUTH_RESTRICTED_USER_ID = 21;
    // SUPERVISOR_USER has only design and manageUsers (on partner) permissions
    private static final int SUPERVISOR_USER_ID = 4;
    // REMOVED_USER used to have permissions but has now had them revoked
    private static final int REVOKED_USER = 5;
    // UNAUTH_USER has no permissions at all
    private static final int UNAUTH_USER_ID = 3;

    private UserDatabaseMeta getDb(int userId) {
        return provider.getDatabaseMetadata(CuidAdapter.databaseId(DB_ID), userId).orElse(null);
    }

    @Test
    public void queryOwnerPermissions() {
        // Owner should be entitled to perform any operation without any record filters
        Operation[] operations = Operation.values();
        for (Operation operation : operations) {
            PermissionQuery query = new PermissionQuery(OWNER_ID, DB_ID, operation, FORM_ID);
            Permission permission = PermissionOracle.query(query, getDb(OWNER_ID));
            assertThat(permission.getOperation(), equalTo(operation));
            assertTrue(permission.isPermitted());
            assertFalse(permission.isFiltered());
        }
    }

    @Test
    public void queryUnauthorizedUser() {
        // An unauthorized user (i.e. one who has no permissions on a database) should be denied permission for _any_
        // operation
        Operation[] operations = Operation.values();
        for (Operation operation : operations) {
            PermissionQuery query = new PermissionQuery(UNAUTH_USER_ID, DB_ID, operation, FORM_ID);
            Permission permission = PermissionOracle.query(query, getDb(UNAUTH_USER_ID));
            assertThat(permission.getOperation(), equalTo(operation));
            assertFalse(permission.isPermitted());
        }
    }

    @Test
    public void queryViewPermission() {
        // Query for authorized user who can view records for any partner
        PermissionQuery query = new PermissionQuery(AUTH_USER_ID, DB_ID, Operation.VIEW, FORM_ID);
        Permission permission = PermissionOracle.query(query, getDb(AUTH_USER_ID));
        assertThat(permission.getOperation(), equalTo(Operation.VIEW));
        assertTrue(permission.isPermitted());
        assertFalse(permission.isFiltered());

        // Query for authorized user who is restricted by partner
        query = new PermissionQuery(AUTH_RESTRICTED_USER_ID, DB_ID, Operation.VIEW, FORM_ID);
        permission = PermissionOracle.query(query, getDb(AUTH_RESTRICTED_USER_ID));
        assertThat(permission.getOperation(), equalTo(Operation.VIEW));
        assertTrue(permission.isPermitted());
        assertTrue(permission.isFiltered());

        // Query for user on database without permissions to view records
        query = new PermissionQuery(REVOKED_USER, DB_ID, Operation.VIEW, FORM_ID);
        permission = PermissionOracle.query(query, getDb(REVOKED_USER));
        assertThat(permission.getOperation(), equalTo(Operation.VIEW));
        assertFalse(permission.isPermitted());
    }

    @Test
    public void queryCreateRecordPermission() {
        // Query for authorized user who can create records for any partner
        PermissionQuery query = new PermissionQuery(AUTH_USER_ID, DB_ID, Operation.CREATE_RECORD, FORM_ID);
        Permission permission = PermissionOracle.query(query, getDb(AUTH_USER_ID));
        assertThat(permission.getOperation(), equalTo(Operation.CREATE_RECORD));
        assertTrue(permission.isPermitted());
        assertFalse(permission.isFiltered());

        // Query for authorized user who is restricted by partner
        query = new PermissionQuery(AUTH_RESTRICTED_USER_ID, DB_ID, Operation.CREATE_RECORD, FORM_ID);
        permission = PermissionOracle.query(query, getDb(AUTH_RESTRICTED_USER_ID));
        assertThat(permission.getOperation(), equalTo(Operation.CREATE_RECORD));
        assertTrue(permission.isPermitted());
        assertTrue(permission.isFiltered());

        // Query for user on database without permissions to create records
        query = new PermissionQuery(SUPERVISOR_USER_ID, DB_ID, Operation.CREATE_RECORD, FORM_ID);
        permission = PermissionOracle.query(query, getDb(SUPERVISOR_USER_ID));
        assertThat(permission.getOperation(), equalTo(Operation.CREATE_RECORD));
        assertFalse(permission.isPermitted());
    }

    @Test
    public void queryEditRecordPermission() {
        // Query for authorized user who can edit records for any partner
        PermissionQuery query = new PermissionQuery(AUTH_USER_ID, DB_ID, Operation.EDIT_RECORD, FORM_ID);
        Permission permission = PermissionOracle.query(query, getDb(AUTH_USER_ID));
        assertThat(permission.getOperation(), equalTo(Operation.EDIT_RECORD));
        assertTrue(permission.isPermitted());
        assertFalse(permission.isFiltered());

        // Query for authorized user who is restricted by partner
        query = new PermissionQuery(AUTH_RESTRICTED_USER_ID, DB_ID, Operation.EDIT_RECORD, FORM_ID);
        permission = PermissionOracle.query(query, getDb(AUTH_RESTRICTED_USER_ID));
        assertThat(permission.getOperation(), equalTo(Operation.EDIT_RECORD));
        assertTrue(permission.isPermitted());
        assertTrue(permission.isFiltered());

        // Query for user on database without permissions to edit records
        query = new PermissionQuery(SUPERVISOR_USER_ID, DB_ID, Operation.EDIT_RECORD, FORM_ID);
        permission = PermissionOracle.query(query, getDb(SUPERVISOR_USER_ID));
        assertThat(permission.getOperation(), equalTo(Operation.EDIT_RECORD));
        assertFalse(permission.isPermitted());
    }

    @Test
    public void queryDeleteRecordPermission() {
        // Query for authorized user who can delete records for any partner
        PermissionQuery query = new PermissionQuery(AUTH_USER_ID, DB_ID, Operation.DELETE_RECORD, FORM_ID);
        Permission permission = PermissionOracle.query(query, getDb(AUTH_USER_ID));
        assertThat(permission.getOperation(), equalTo(Operation.DELETE_RECORD));
        assertTrue(permission.isPermitted());
        assertFalse(permission.isFiltered());

        // Query for authorized user who is restricted by partner
        query = new PermissionQuery(AUTH_RESTRICTED_USER_ID, DB_ID, Operation.DELETE_RECORD, FORM_ID);
        permission = PermissionOracle.query(query, getDb(AUTH_RESTRICTED_USER_ID));
        assertThat(permission.getOperation(), equalTo(Operation.DELETE_RECORD));
        assertTrue(permission.isPermitted());
        assertTrue(permission.isFiltered());

        // Query for user on database without permissions to delete records
        query = new PermissionQuery(SUPERVISOR_USER_ID, DB_ID, Operation.DELETE_RECORD, FORM_ID);
        permission = PermissionOracle.query(query, getDb(SUPERVISOR_USER_ID));
        assertThat(permission.getOperation(), equalTo(Operation.DELETE_RECORD));
        assertFalse(permission.isPermitted());
    }

    @Test
    public void queryCreateFormPermission() {
        // Query for authorized user who can create forms on database
        PermissionQuery query = new PermissionQuery(SUPERVISOR_USER_ID, DB_ID, Operation.CREATE_RESOURCE, CuidAdapter.databaseId(DB_ID));
        Permission permission = PermissionOracle.query(query, getDb(SUPERVISOR_USER_ID));
        assertThat(permission.getOperation(), equalTo(Operation.CREATE_RESOURCE));
        assertTrue(permission.isPermitted());
        assertFalse(permission.isFiltered());

        // Query for user on database without permissions to create forms
        query = new PermissionQuery(AUTH_RESTRICTED_USER_ID, DB_ID, Operation.CREATE_RESOURCE, CuidAdapter.databaseId(DB_ID));
        permission = PermissionOracle.query(query, getDb(AUTH_RESTRICTED_USER_ID));
        assertThat(permission.getOperation(), equalTo(Operation.CREATE_RESOURCE));
        assertFalse(permission.isPermitted());
    }

    @Test
    public void queryEditFormPermission() {
        // Query for authorized user who can create forms on database
        PermissionQuery query = new PermissionQuery(SUPERVISOR_USER_ID, DB_ID, Operation.EDIT_RESOURCE, FORM_ID);
        Permission permission = PermissionOracle.query(query, getDb(SUPERVISOR_USER_ID));
        assertThat(permission.getOperation(), equalTo(Operation.EDIT_RESOURCE));
        assertTrue(permission.isPermitted());
        assertFalse(permission.isFiltered());

        // Query for user on database without permissions to create forms
        query = new PermissionQuery(AUTH_RESTRICTED_USER_ID, DB_ID, Operation.EDIT_RESOURCE, FORM_ID);
        permission = PermissionOracle.query(query, getDb(AUTH_RESTRICTED_USER_ID));
        assertThat(permission.getOperation(), equalTo(Operation.EDIT_RESOURCE));
        assertFalse(permission.isPermitted());
    }

    @Test
    public void queryDeleteFormPermission() {
        // Query for authorized user who can create forms on database
        PermissionQuery query = new PermissionQuery(SUPERVISOR_USER_ID, DB_ID, Operation.DELETE_RESOURCE, FORM_ID);
        Permission permission = PermissionOracle.query(query, getDb(SUPERVISOR_USER_ID));
        assertThat(permission.getOperation(), equalTo(Operation.DELETE_RESOURCE));
        assertTrue(permission.isPermitted());
        assertFalse(permission.isFiltered());

        // Query for user on database without permissions to create forms
        query = new PermissionQuery(AUTH_RESTRICTED_USER_ID, DB_ID, Operation.DELETE_RESOURCE, FORM_ID);
        permission = PermissionOracle.query(query, getDb(AUTH_RESTRICTED_USER_ID));
        assertThat(permission.getOperation(), equalTo(Operation.DELETE_RESOURCE));
        assertFalse(permission.isPermitted());
    }

    @Test
    public void queryManageUsersPermission() {
        // Query for authorized user who can manage users for any partner
        PermissionQuery query = new PermissionQuery(AUTH_USER_ID, DB_ID, Operation.MANAGE_USERS, FORM_ID);
        Permission permission = PermissionOracle.query(query, getDb(AUTH_USER_ID));
        assertThat(permission.getOperation(), equalTo(Operation.MANAGE_USERS));
        assertTrue(permission.isPermitted());
        assertFalse(permission.isFiltered());

        // Query for authorized user who is restricted by partner
        query = new PermissionQuery(SUPERVISOR_USER_ID, DB_ID, Operation.MANAGE_USERS, FORM_ID);
        permission = PermissionOracle.query(query, getDb(SUPERVISOR_USER_ID));
        assertThat(permission.getOperation(), equalTo(Operation.MANAGE_USERS));
        assertTrue(permission.isPermitted());
        assertTrue(permission.isFiltered());

        // Query for user on database without permissions to manage users
        query = new PermissionQuery(AUTH_RESTRICTED_USER_ID, DB_ID, Operation.MANAGE_USERS, FORM_ID);
        permission = PermissionOracle.query(query, getDb(AUTH_RESTRICTED_USER_ID));
        assertThat(permission.getOperation(), equalTo(Operation.MANAGE_USERS));
        assertFalse(permission.isPermitted());
    }

}