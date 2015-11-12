package org.activityinfo.server.authentication;

import com.google.common.base.Charsets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Details of a Humanitarian.id account
 */
public class HumanitarianIdAccount {

    private String id;
    private String email;
    private String givenName;
    private String familyName;
    private boolean active;
    
    public static HumanitarianIdAccount parse(byte[] response) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = (JsonObject)jsonParser.parse(new String(response, Charsets.UTF_8));
        HumanitarianIdAccount account = new HumanitarianIdAccount();
        account.id = jsonObject.get("user_id").getAsString();
        account.email = jsonObject.get("email").getAsString();
        account.familyName = jsonObject.get("name_family").getAsString();
        account.givenName = jsonObject.get("name_given").getAsString();

        return account;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public boolean isActive() {
        return active;
    }
}
