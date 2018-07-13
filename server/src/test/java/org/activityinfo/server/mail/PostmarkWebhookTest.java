package org.activityinfo.server.mail;

import com.google.inject.Inject;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.fixtures.Modules;
import org.activityinfo.server.command.CommandTestCase;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.server.database.hibernate.entity.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(InjectionSupport.class)
@Modules(MailSenderStubModule.class)
@OnDataSet("/dbunit/clone-database.db.xml")
public class PostmarkWebhookTest extends CommandTestCase {

    @Inject
    private PostmarkWebhook postmarkWebhook;

    private BounceReport bounceReport;

    @Before
    public void setUp() {
        this.bounceReport = new BounceReport();
        bounceReport.setRecordType("Bounce");
        bounceReport.setEmail("akbertram@gmail.com");
    }

    @Test
    public void bounceEmail() {
        // Get user with email notifications enabled
        User user = em.find(User.class, 1);
        assert(user.isEmailNotification());

        // Bounce email
        postmarkWebhook.bounce("test_token", bounceReport);

        // Ensure user now has email notifications disabled
        user = em.find(User.class, 1);
        assert(!user.isEmailNotification());
    }

}