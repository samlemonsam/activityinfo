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
package org.activityinfo.test.sut;

import org.activityinfo.test.config.ConfigProperty;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Provides information about and access to the deployment of the
 * server currently under test
 */
public class Server {


    public static final ConfigProperty TEST_URL = new ConfigProperty("test.url", "Root URL to Test");

    private static final String LOCAL_URL = "http://localhost:8080/";
    
    private final String rootUrl;

    public Server() {
        this.rootUrl = TEST_URL.getOr(LOCAL_URL);
    }
    
    public Server(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    public String getRootUrl() {
        return path("");
    }
    
    public URI getRootUri() {
        try {
            return new URI(getRootUrl());
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    public String path(String path) {
        String url = rootUrl;
        if(!url.endsWith("/")) {
            url += "/";
        }
        if(path.startsWith("/")) {
            url += path.substring(1);
        } else {
            url += path;
        }
        return url;
    }

    /**
     *
     * @return the URL of the application
     */
    public String appUrl() {
        return path("");
    }
}
