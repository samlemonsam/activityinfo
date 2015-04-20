package org.activityinfo.test.driver.mail;

import java.util.ArrayList;
import java.util.List;

public class Inbox {
    private final List<MessageHeader> headers = new ArrayList<>();

    public List<MessageHeader> getMessages() {
        return headers;
    }
}
