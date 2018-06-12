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

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;

import static org.easymock.EasyMock.*;

public class RestMockUtils {

    public static UriInfo mockUriInfo(String uri) throws URISyntaxException {
        UriInfo uriInfo = createMock(UriInfo.class);
        expect(uriInfo.getRequestUri()).andReturn(new URI(uri)).anyTimes();
        expect(uriInfo.getAbsolutePathBuilder()).andReturn(
                UriBuilder.fromUri(uri));
        replay(uriInfo);
        return uriInfo;
    }

}
