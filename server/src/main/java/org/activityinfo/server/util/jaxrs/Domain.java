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

import java.io.Serializable;

/**
 * Provides metadata about the domain on which the application is being served.
 */
public class Domain implements Serializable {
    private static final long serialVersionUID = 241542892559897521L;


    public static final String DEFAULT_HOST = "www.activityinfo.org";
    public static final String DEFAULT_TITLE = "ActivityInfo";
    public static final Domain DEFAULT = new Domain();

    private final String host;
    private final int port;

    public Domain() {
        this(DEFAULT_HOST, 443);
    }

    public Domain(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getTitle() {
        return DEFAULT_TITLE;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        if(port == 0) {
            throw new IllegalStateException("port == 0");
        }
        return port;
    }

    /**
     * @return the root url for this domain, including only protocal (http/https),
     * host, and port number if neccessary.
     */
    public String getRootUrl() {
        StringBuilder sb = new StringBuilder();
        if(host.equals("localhost")) {
            sb.append("http://localhost:").append(port);
        } else {
            // Force HTTPS and drop port number
            sb.append("https://").append(host);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return host;
    }
}
