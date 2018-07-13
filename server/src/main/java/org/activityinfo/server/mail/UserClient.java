package org.activityinfo.server.mail;

import org.activityinfo.json.JsonValue;

public class UserClient {

    private String name;
    private String company;
    private String family;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public static UserClient fromJson(JsonValue object) {
        UserClient client = new UserClient();
        client.setName(object.get("Name").asString());
        client.setCompany(object.get("Company").asString());
        client.setFamily(object.get("Family").asString());
        return client;
    }

}
