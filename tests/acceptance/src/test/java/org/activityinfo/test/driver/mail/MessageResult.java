package org.activityinfo.test.driver.mail;


import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageResult {
    
    private Message data;

    public Message getData() {
        return data;
    }

    public void setData(Message data) {
        this.data = data;
    }
}
