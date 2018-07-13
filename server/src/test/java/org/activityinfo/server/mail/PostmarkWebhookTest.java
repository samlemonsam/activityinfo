package org.activityinfo.server.mail;

import com.google.inject.Inject;
import com.google.inject.util.Providers;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.fixtures.Modules;
import org.activityinfo.fixtures.TestHibernateModule;
import org.activityinfo.server.DeploymentConfiguration;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.util.config.ConfigModuleStub;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;

import static org.junit.Assert.assertTrue;

@RunWith(InjectionSupport.class)
@Modules({
        TestHibernateModule.class,
        ConfigModuleStub.class
})
@OnDataSet("/dbunit/clone-database.db.xml")
public class PostmarkWebhookTest {

    @Inject
    protected EntityManager em;

    @Inject
    protected DeploymentConfiguration config;

    private PostmarkWebhook postmarkWebhook;

    @Before
    public void setUp() {
        postmarkWebhook = new PostmarkWebhook(config, Providers.of(em));
    }

    @Test
    public void bounceEmail() {
        // Get user with email notifications enabled
        User user = em.find(User.class, 1);
        assertTrue(user.isEmailNotification());

        // Bounce email
        BounceReport bounceReport = new BounceReport();
        bounceReport.setRecordType("Bounce");
        bounceReport.setEmail("akbertram@gmail.com");
        postmarkWebhook.bounce("test_token", bounceReport);

        // Ensure user now has email notifications disabled
        user = em.find(User.class, 1);
        assertTrue(!user.isEmailNotification());
    }

}
