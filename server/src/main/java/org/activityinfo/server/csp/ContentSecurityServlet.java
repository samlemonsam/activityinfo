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
package org.activityinfo.server.csp;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.activityinfo.json.JsonValue;
import org.activityinfo.server.authentication.ServerSideAuthProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.logging.Logger;

import static org.activityinfo.json.Json.parse;

/**
 * Receives reports of Content Security Policy violations
 * http://www.html5rocks.com/en/tutorials/security/content-security-policy/#reporting
 */
@Singleton
public class ContentSecurityServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(ContentSecurityPolicy.class.getName());

    private final ServerSideAuthProvider authProvider;

    @Inject
    public ContentSecurityServlet(ServerSideAuthProvider authProvider) {
        this.authProvider = authProvider;
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {


        String requestJson = CharStreams.toString(new InputStreamReader(req.getInputStream(), Charsets.UTF_8));
        JsonValue request = parse(requestJson);
        JsonValue report = request.get("csp-report");

        StringBuilder message = new StringBuilder();
        
        if(authProvider.isAuthenticated()) {
            message.append("User: ").append(authProvider.get().getEmail());
        } else {
            message.append("User: ").append("Not authenticated.");
        }
        
        message.append("Content-Security Violation\n");

        for (Map.Entry<String, JsonValue> entry : report.entrySet()) {
            message.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        LOGGER.severe(message.toString());
    }
}
