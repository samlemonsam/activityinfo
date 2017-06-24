package org.activityinfo.test.driver.mail.mailinator;

import org.junit.Test;

import static org.activityinfo.test.driver.mail.mailinator.MessageHeader.parseEmail;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class MessageHeaderTest {

    @Test
    public void testEmail() {
        assertThat(parseEmail("\"ActivityInfo Notifications\" <notifications@activityinfo.org> "), equalTo("notifications@activityinfo.org"));
        assertThat(parseEmail("ActivityInfo Notifications <bob@gmail.com>"), equalTo("bob@gmail.com"));
        assertThat(parseEmail("bob@gmail.com"), equalTo("bob@gmail.com"));

    }
    
    

}