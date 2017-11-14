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
