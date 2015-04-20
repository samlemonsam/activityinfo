package org.activityinfo.test.driver.mail;

import org.activityinfo.test.sut.UserAccount;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class MailinatorClientTest {

    @Test
    public void test() throws IOException {
        
        MailinatorClient client = new MailinatorClient("6f6f3b5dac2b43e28aeae416c0b9d072");
        List<MessageHeader> headers = client.queryInbox(new UserAccount("foobar@mailinator.com", "xyz"));

        Message message = client.queryMessage(headers.get(0));
        
        System.out.println(message.getPlainText());


    }
    
}