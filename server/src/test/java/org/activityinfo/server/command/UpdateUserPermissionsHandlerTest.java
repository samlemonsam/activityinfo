/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.server.command;

import com.google.inject.util.Providers;
import freemarker.template.TemplateModelException;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.fixtures.MockDb;
import org.activityinfo.fixtures.Modules;
import org.activityinfo.legacy.shared.command.GetUsers;
import org.activityinfo.legacy.shared.command.UpdateUserPermissions;
import org.activityinfo.legacy.shared.command.result.UserResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.exception.IllegalAccessCommandException;
import org.activityinfo.legacy.shared.model.FolderDTO;
import org.activityinfo.legacy.shared.model.PartnerDTO;
import org.activityinfo.legacy.shared.model.UserPermissionDTO;
import org.activityinfo.server.command.handler.UpdateUserPermissionsHandler;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.server.database.hibernate.dao.PartnerDAO;
import org.activityinfo.server.database.hibernate.dao.UserDAO;
import org.activityinfo.server.database.hibernate.dao.UserDatabaseDAO;
import org.activityinfo.server.database.hibernate.dao.UserPermissionDAO;
import org.activityinfo.server.database.hibernate.entity.Database;
import org.activityinfo.server.database.hibernate.entity.Partner;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.database.hibernate.entity.UserPermission;
import org.activityinfo.server.endpoint.rest.BillingAccountOracle;
import org.activityinfo.server.mail.MailSenderStub;
import org.activityinfo.server.mail.MailSenderStubModule;
import org.activityinfo.server.util.TemplateModule;
import org.activityinfo.server.util.jaxrs.Domain;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(InjectionSupport.class)
@Modules({MailSenderStubModule.class})
public class UpdateUserPermissionsHandlerTest extends CommandTestCase {

    private Partner NRC;
    private Partner IRC;
    private PartnerDTO NRC_DTO;

    private final MockDb db = new MockDb();
    private MailSenderStub mailer;
    private UpdateUserPermissionsHandler handler;
    private User owner;

    @Before
    public void setup() throws TemplateModelException {
        NRC = new Partner();
        NRC.setId(1);
        NRC.setName("NRC");
        NRC.setFullName("Norwegian Refugee Council");
        db.persist(NRC);

        IRC = new Partner();
        IRC.setId(2);
        IRC.setName("IRC");
        IRC.setFullName("International Rescue Committee");
        db.persist(IRC);

        NRC_DTO = new PartnerDTO(1, "NRC");

        TemplateModule templateModule = new TemplateModule();
        mailer = new MailSenderStub(templateModule.provideConfiguration(Providers.of(Domain.DEFAULT)));

        handler = new UpdateUserPermissionsHandler(
                db.getDAO(UserDatabaseDAO.class), db.getDAO(PartnerDAO.class),
                db.getDAO(UserDAO.class),
                db.getDAO(UserPermissionDAO.class), new BillingAccountOracle(Providers.of(em)), mailer);

        owner = new User();
        owner.setId(99);
        owner.setName("Alex");
        owner.setEmail("alex@bedatadriven.com");
        db.persist(owner);

        Database udb = new Database(1, "PEAR");
        udb.setOwner(owner);
        db.persist(udb);
    }

    @Test
    public void ownerCanAddUser() throws Exception {

        UserPermissionDTO user = new UserPermissionDTO();
        user.setEmail("other@foobar");
        user.setName("Foo Bar");
        user.addUserGroup(NRC_DTO);
        user.setAllowView(true);

        UpdateUserPermissions cmd = new UpdateUserPermissions(1, user);

        handler.execute(cmd, owner);

        assertThat(mailer.sentMails.size(), equalTo(1));
    }

    /**
     * Asserts that someone with ManageUsersPermission will be permitted to
     * grant some one edit rights.
     */
    @Test
    public void testVerifyAuthorityForViewPermissions()
            throws IllegalAccessCommandException {

        UserPermission executingUserPermissions = new UserPermission();
        executingUserPermissions.addUserGroup(NRC);
        executingUserPermissions.setAllowManageUsers(true);

        UserPermissionDTO dto = new UserPermissionDTO();
        dto.addUserGroup(NRC_DTO);
        dto.setAllowView(true);

        UpdateUserPermissions cmd = new UpdateUserPermissions(1, dto);

        UpdateUserPermissionsHandler.verifyAuthority(cmd, executingUserPermissions);
    }

    /**
     * Asserts that someone with ManageUsersPermission will be permitted to
     * grant some one edit rights.
     */
    @Test
    public void testVerifyAuthorityForEditPermissions()
            throws IllegalAccessCommandException {

        UserPermission executingUserPermissions = new UserPermission();
        executingUserPermissions.addUserGroup(NRC);
        executingUserPermissions.setAllowManageUsers(true);

        UserPermissionDTO dto = new UserPermissionDTO();
        dto.addUserGroup(NRC_DTO);
        dto.setAllowView(true);
        dto.setAllowEdit(true);

        UpdateUserPermissions cmd = new UpdateUserPermissions(1, dto);

        UpdateUserPermissionsHandler.verifyAuthority(cmd, executingUserPermissions);
    }

    @Test(expected = IllegalAccessCommandException.class)
    public void testFailingVerifyAuthorityForView()
            throws IllegalAccessCommandException {

        UserPermission executingUserPermissions = new UserPermission();
        executingUserPermissions.addUserGroup(IRC);
        executingUserPermissions.setAllowManageUsers(true);

        UserPermissionDTO dto = new UserPermissionDTO();
        dto.addUserGroup(NRC_DTO);
        dto.setAllowView(true);
        dto.setAllowEdit(true);

        UpdateUserPermissions cmd = new UpdateUserPermissions(1, dto);

        UpdateUserPermissionsHandler.verifyAuthority(cmd, executingUserPermissions);
    }

    @Test
    public void testVerifyAuthorityForViewByOtherPartner()
            throws IllegalAccessCommandException {

        UserPermission executingUserPermissions = new UserPermission();
        executingUserPermissions.addUserGroup(IRC);
        executingUserPermissions.setAllowManageUsers(true);
        executingUserPermissions.setAllowManageAllUsers(true);

        UserPermissionDTO dto = new UserPermissionDTO();
        dto.addUserGroup(NRC_DTO);
        dto.setAllowView(true);
        dto.setAllowEdit(true);

        UpdateUserPermissions cmd = new UpdateUserPermissions(1, dto);

        UpdateUserPermissionsHandler.verifyAuthority(cmd, executingUserPermissions);
    }

    /**
     * Verifies that a user with the manageUsers permission can add another user to the UserDatabase
     *
     */
    @Test
    @OnDataSet("/dbunit/schema1.db.xml")
    public void testAuthorizedCreate() throws CommandException {

        setUser(2);

        UserPermissionDTO user = new UserPermissionDTO();
        user.setEmail("ralph@lauren.com");
        user.setName("Ralph");
        user.addUserGroup(new PartnerDTO(1, "NRC"));
        user.setAllowView(true);
        user.setAllowEdit(true);

        UpdateUserPermissions cmd = new UpdateUserPermissions(1, user);
        execute(cmd);

        UserResult result = execute(new GetUsers(1));
        assertThat(result.getTotalLength(), equalTo(1));

        UserPermissionDTO ralph = result.getData().get(0);
        assertThat(ralph.getEmail(), equalTo("ralph@lauren.com"));
        assertThat(ralph.getAllowEdit(), equalTo(true));
        assertThat(ralph.hasFolderLimitation(), equalTo(false));
    }


    /**
     * Verifies that the owner of a database can update an existing users permission
     */
    @Test
    @OnDataSet("/dbunit/schema1.db.xml")
    public void testOwnerUpdate() throws CommandException {
        setUser(1);

        UserPermissionDTO user = new UserPermissionDTO();
        user.setEmail("bavon@nrcdrc.org");
        user.addUserGroup(new PartnerDTO(1, "NRC"));
        user.setAllowView(true);
        user.setAllowViewAll(false);
        user.setAllowEdit(true);
        user.setAllowEdit(false);
        user.setAllowDesign(true);

        execute(new UpdateUserPermissions(1, user));

        UserResult result = execute(new GetUsers(1));

        UserPermissionDTO reUser = result.getData().get(0);
        assertThat(reUser.getEmail(), equalTo("bavon@nrcdrc.org"));
        assertThat(reUser.getAllowDesign(), equalTo(true));
    }

    @Test
    @OnDataSet("/dbunit/schema3.db.xml")
    public void testFolderLevelUpdate() {
        setUser(1);

        UserPermissionDTO newUser = new UserPermissionDTO();
        newUser.setName("Bavon");
        newUser.setEmail("bavon@nrcdrc.org");
        newUser.addUserGroup(new PartnerDTO(1, "NRC"));
        newUser.setAllowView(true);
        newUser.setAllowViewAll(false);
        newUser.setAllowEdit(true);
        newUser.setAllowEdit(false);
        newUser.setAllowDesign(true);

        FolderDTO health = new FolderDTO();
        health.setId(3);

        newUser.setFolders(Arrays.asList(health));
        newUser.setFolderLimitation(true);

        execute(new UpdateUserPermissions(1, newUser));

        UserResult users = execute(new GetUsers(1));
        UserPermissionDTO bavon = users.getData().get(0);

        assertThat(bavon.hasFolderLimitation(), equalTo(true));

    }
}
