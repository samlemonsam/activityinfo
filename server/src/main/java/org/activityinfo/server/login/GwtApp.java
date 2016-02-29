package org.activityinfo.server.login;

import com.bedatadriven.rebar.appcache.server.UserAgentProvider;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import javax.servlet.ServletContext;
import javax.ws.rs.core.StreamingOutput;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


public class GwtApp {
    
    private static final Logger LOGGER = Logger.getLogger(GwtApp.class.getName());

    private String moduleBase;

    public GwtApp(ServletContext servletContext, String moduleBase) {
        this.moduleBase = moduleBase;
    }

    private JsonArray readPermutationMap() {
        try {
            InputStreamReader reader =
                    new InputStreamReader(
                            new FileInputStream(
                                    getServletContext().getRealPath(moduleBase + "permutations")));

            JsonParser parser = new JsonParser();
            return (JsonArray) parser.parse(reader);

        } catch (FileNotFoundException e) {
            LOGGER.info("No permutations map found, (we are probably in dev mode) will return default selection script");
            return null;
        }
    }
    
    private StreamingOutput getScript(String userAgentHeader, String locale) {
        Map<String, String> properties = new HashMap<>();
        properties.put("user.agent", UserAgentProvider)
    }
    
}
