package org.activityinfo.server.command.handler;

import com.google.inject.Inject;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.fixtures.Modules;
import org.activityinfo.fixtures.TestHibernateModule;
import org.activityinfo.model.database.Operation;
import org.activityinfo.model.database.Permission;
import org.activityinfo.model.database.PermissionQuery;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.database.OnDataSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

@RunWith(InjectionSupport.class)
@Modules({
        TestHibernateModule.class,
})
@OnDataSet("/dbunit/sites-simple1.db.xml")
public class PermissionOracleTest {

    private static final int DB_ID = 2;
    private static final ResourceId FORM_ID = CuidAdapter.activityFormClass(3);

    private static final int OWNER_ID = 1;
    private static final int AUTH_USER_ID = 2;
    private static final int UNAUTH_USER_ID = 3;
    private static final int AUTH_RESTRICTED_USER_ID = 21;

    @Inject
    private EntityManager em;

    private PermissionOracle oracle;

    @Before
    public void setUp() {
        this.oracle = new PermissionOracle(em);
    }

    @Test
    public void queryOwnerPermissions() {
        // Owner should be entitled to perform any operation without any record filters
        Operation[] operations = Operation.values();
        for (Operation operation : operations) {
            PermissionQuery query = new PermissionQuery(OWNER_ID, DB_ID, operation, FORM_ID);
            Permission permission = oracle.query(query);
            assertThat(permission.getOperation(), equalTo(operation));
            assertTrue(permission.isPermitted());
            assertFalse(permission.getFilter().isPresent());
        }
    }

    @Test
    public void queryUnauthorizedUser() {
        // An unauthorized user (i.e. one who has no permissions on a database) should be denied permission for _any_
        // operation
        Operation[] operations = Operation.values();
        for (Operation operation : operations) {
            PermissionQuery query = new PermissionQuery(UNAUTH_USER_ID, DB_ID, operation, FORM_ID);
            Permission permission = oracle.query(query);
            assertThat(permission.getOperation(), equalTo(operation));
            assertFalse(permission.isPermitted());
        }
    }

    @Test
    public void queryViewPermission() {
        // Query for authorised user
        PermissionQuery query = new PermissionQuery(AUTH_USER_ID, DB_ID, Operation.VIEW, FORM_ID);
        Permission permission = oracle.query(query);
        assertThat(permission.getOperation(), equalTo(Operation.VIEW));
        assertTrue(permission.isPermitted());

        // Query for unauthorized user
        query = new PermissionQuery(UNAUTH_USER_ID, DB_ID, Operation.VIEW, FORM_ID);
        permission = oracle.query(query);
        assertThat(permission.getOperation(), equalTo(Operation.VIEW));
        assertFalse(permission.isPermitted());
    }

    @Test
    public void queryCreateRecordPermission() {
        // Query for authorized user who can create records for any partner
        PermissionQuery query = new PermissionQuery(AUTH_USER_ID, DB_ID, Operation.CREATE_RECORD, FORM_ID);
        Permission permission = oracle.query(query);
        assertThat(permission.getOperation(), equalTo(Operation.CREATE_RECORD));
        assertTrue(permission.isPermitted());
        assertFalse(permission.getFilter().isPresent());

        // Query for authorised user who is restricted by partner
        query = new PermissionQuery(AUTH_RESTRICTED_USER_ID, DB_ID, Operation.CREATE_RECORD, FORM_ID);
        permission = oracle.query(query);
        assertThat(permission.getOperation(), equalTo(Operation.CREATE_RECORD));
        assertTrue(permission.isPermitted());
        assertTrue(permission.getFilter().isPresent());

        // Query for unauthorized user
        query = new PermissionQuery(UNAUTH_USER_ID, DB_ID, Operation.CREATE_RECORD, FORM_ID);
        permission = oracle.query(query);
        assertThat(permission.getOperation(), equalTo(Operation.CREATE_RECORD));
        assertFalse(permission.isPermitted());
    }

    @Test
    public void queryEditRecordPermission() {
        // Query for authorized user who can edit records for any partner
        PermissionQuery query = new PermissionQuery(AUTH_USER_ID, DB_ID, Operation.EDIT_RECORD, FORM_ID);
        Permission permission = oracle.query(query);
        assertThat(permission.getOperation(), equalTo(Operation.EDIT_RECORD));
        assertTrue(permission.isPermitted());
        assertFalse(permission.getFilter().isPresent());

        // Query for authorised user can edit records but is restricted by partner
        query = new PermissionQuery(AUTH_RESTRICTED_USER_ID, DB_ID, Operation.EDIT_RECORD, FORM_ID);
        permission = oracle.query(query);
        assertThat(permission.getOperation(), equalTo(Operation.EDIT_RECORD));
        assertTrue(permission.isPermitted());
        assertTrue(permission.getFilter().isPresent());

        // Query for unauthorized user
        query = new PermissionQuery(UNAUTH_USER_ID, DB_ID, Operation.EDIT_RECORD, FORM_ID);
        permission = oracle.query(query);
        assertThat(permission.getOperation(), equalTo(Operation.EDIT_RECORD));
        assertFalse(permission.isPermitted());
    }

}