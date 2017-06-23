package org.activityinfo.server.csp;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.activityinfo.json.JsonObject;
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
        Gson gson = new Gson();
        JsonObject request = gson.fromJson(new InputStreamReader(req.getInputStream(), Charsets.UTF_8),
                JsonObject.class);

        JsonObject report = request.get("csp-report").getAsJsonObject();

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
