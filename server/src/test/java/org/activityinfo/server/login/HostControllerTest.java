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
package org.activityinfo.server.login;

import com.google.inject.util.Providers;
import com.sun.jersey.api.view.Viewable;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.server.DeploymentConfiguration;
import org.activityinfo.server.authentication.ServerSideAuthProvider;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.login.model.HostPageModel;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.util.Properties;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public class HostControllerTest extends ControllerTestCase {

    private static final String CHROME_USER_AGENT = "Mozilla/6.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/532.0 (KHTML, like Gecko) Chrome/3.0.195.27 Safari/532.0";
    private static final String VALID_TOKEN = "XYZ123";

    private HostController resource;
    private ServerSideAuthProvider authProvider;
    private EntityManager entityManager;

    @Before
    public void setup() {
        DeploymentConfiguration deploymentConfig = new DeploymentConfiguration(
                new Properties());

        authProvider = new ServerSideAuthProvider();
        authProvider.clear();

        entityManager = createMock(EntityManager.class);
        expect(entityManager.find(User.class, 3)).andReturn(new User()).anyTimes();
        replay(entityManager);

        resource = new HostController(authProvider, Providers.of(entityManager));
    }

    @Test
    public void verifyThatRequestsWithoutAuthTokensAreShownLoginPage()
            throws Exception {

        // Request for main page
        HttpServletRequest req = createMock(HttpServletRequest.class);
        expect(req.getServerName()).andReturn("www.activityinfo.org");
        expect(req.getHeader("User-Agent")).andReturn(CHROME_USER_AGENT);
        replay(req);

        Response response = resource.getHostPage(
                RestMockUtils.mockUriInfo("http://www.activityinfo.org/app"), req,
                "oldui", null, null, null);

        assertThat(response.getStatus(), equalTo(Response.Status.TEMPORARY_REDIRECT.getStatusCode()));
    }

    @Test
    public void verifyThatRequestWithValidAuthTokensReceiveTheView()
            throws Exception {

        authProvider.set(new AuthenticatedUser(VALID_TOKEN, 3, "akbertram@gmail.com"));

        HttpServletRequest req = createMock(HttpServletRequest.class);
        expect(req.getServerName()).andReturn("www.activityinfo.org");
        expect(req.getHeader("User-Agent")).andReturn(CHROME_USER_AGENT);
        replay(req);

        Response response = resource.getHostPage(
                RestMockUtils.mockUriInfo("http://www.activityinfo.org"), req,
                "oldui", null, null, null);

        assertThat(response.getEntity(), instanceOf(Viewable.class));
        assertThat(((Viewable) response.getEntity()).getModel(),
                instanceOf(HostPageModel.class));
    }

}
