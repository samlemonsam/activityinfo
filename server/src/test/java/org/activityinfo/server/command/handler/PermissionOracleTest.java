package org.activityinfo.server.command.handler;

import com.google.inject.Inject;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.fixtures.Modules;
import org.activityinfo.fixtures.TestHibernateModule;
import org.activityinfo.model.database.Operation;
import org.activityinfo.model.database.Permission;
import org.activityinfo.model.database.PermissionQuery;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.server.database.OnDataSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManagerFactory;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

@RunWith(InjectionSupport.class)
@Modules({
        TestHibernateModule.class,
})
@OnDataSet("/dbunit/sites-simple1.db.xml")
public class PermissionOracleTest {

    @Inject
    private EntityManagerFactory emf;

    private PermissionOracle oracle;

    @Before
    public void setUp() {
        this.oracle = new PermissionOracle(emf.createEntityManager());
    }

    @Test
    public void queryOwnerPermissions() {
        // Owner should be entitled to perform any operation without any record filters
        Operation[] operations = Operation.values();
        for (Operation operation : operations) {
            PermissionQuery query = new PermissionQuery(1,2,operation,CuidAdapter.activityFormClass(3));
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
            PermissionQuery query = new PermissionQuery(4,2,operation,CuidAdapter.activityFormClass(3));
            Permission permission = oracle.query(query);
            assertThat(permission.getOperation(), equalTo(operation));
            assertFalse(permission.isPermitted());
        }
    }

    @Test
    public void queryViewPermission() {
        PermissionQuery query = new PermissionQuery(1, 2, Operation.VIEW, CuidAdapter.activityFormClass(3));
        Permission permission = oracle.query(query);
        assertThat(permission.getOperation(), equalTo(Operation.VIEW));
        assertTrue(permission.isPermitted());
    }


}