package org.activityinfo.server.authentication;

import com.google.common.base.Charsets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.logging.Logger;

/**
 * Details of a Humanitarian.id account
 */
public class HumanitarianIdAccount {

    private static final Logger LOGGER = Logger.getLogger(HumanitarianId.class.getName());

    private String id;
    private String email;
    private String name;
    private boolean active;
    
    public static HumanitarianIdAccount parse(byte[] response) {
        String json = new String(response, Charsets.UTF_8);

        LOGGER.info("Account = " + json);

        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = (JsonObject)jsonParser.parse(json);
        HumanitarianIdAccount account = new HumanitarianIdAccount();
        account.id = jsonObject.get("user_id").getAsString();
        account.email = jsonObject.get("email").getAsString();
        if(jsonObject.has("name")) {
            account.name = jsonObject.get("name").getAsString();
        }
        return account;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return active;
    }

    public String getName() {
        return name;
    }
}
