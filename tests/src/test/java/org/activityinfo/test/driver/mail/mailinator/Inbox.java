package org.activityinfo.test.driver.mail.mailinator;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
class Inbox {

    private final List<MessageHeader> headers = new ArrayList<>();

    public List<MessageHeader> getMessages() {
        return headers;
    }
}
