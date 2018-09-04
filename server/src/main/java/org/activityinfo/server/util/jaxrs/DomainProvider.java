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
package org.activityinfo.server.util.jaxrs;

import com.google.inject.Inject;
import com.google.inject.Provider;

import javax.servlet.http.HttpServletRequest;

import static com.google.common.base.Strings.emptyToNull;

/**
 * Provides information on the domain branding to use based
 * on this thread's current request.
 */
public class DomainProvider implements Provider<Domain> {

    private final Provider<HttpServletRequest> request;

    @Inject
    public DomainProvider(Provider<HttpServletRequest> request) {
        super();
        this.request = request;
    }

    @Override
    public Domain get() {
        return new Domain(getExternalHostName(), request.get().getServerPort());
    }

    private String getExternalHostName() {

        String requestHost = request.get().getServerName();

        // Requests initiated by the task service will use
        // the internal name. For our production server, pretty it up.
        if(requestHost.equals("activityinfoeu.appspot.com")) {
            return "www.activityinfo.org";
        }
        return requestHost;
    }

    /**
     *
     * Return the hostname to use for looking up the branded domain.
     *
     * If the request is forwarded from a proxy server, this host name might
     * be different from both the requested host name ('proxy.default.activityinfoeu.appspot.com')
     * and the host name requested by the end user ('proxy.activityinfo.org') if we are
     * are setting up an alias to an existing host.
     *
     * @return the host name to use for looking up the branded version of AI to serve.
     *
     */
    private String getBrandHostName() {

        String host = getHeader("X-AI-Domain");
        if(host != null) {
            return host;
        }
        host = getHeader("X-Forwarded-Host");
        if(host != null) {
            return host;
        }
        return request.get().getServerName();
    }

    private String getHeader(String headerName) {
        return emptyToNull(request.get().getHeader(headerName));
    }
}
