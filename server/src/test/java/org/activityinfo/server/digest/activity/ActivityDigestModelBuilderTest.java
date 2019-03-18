package org.activityinfo.server.digest.activity;

import com.bedatadriven.rebar.time.calendar.LocalDate;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.fixtures.Modules;
import org.activityinfo.fixtures.TestHibernateModule;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.server.database.hibernate.entity.Database;
import org.activityinfo.server.database.hibernate.entity.Partner;
import org.activityinfo.server.database.hibernate.entity.SiteHistory;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.digest.TestDigestModule;
import org.activityinfo.server.digest.UserDigest;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/digests.db.xml")
@Modules({
        TestHibernateModule.class,
        TestDigestModule.class
})
public class ActivityDigestModelBuilderTest {

    @Inject
    ActivityDigestModelBuilder activityDigestModelBuilder;

    @Inject
    EntityManager em;

    @Test
    public void testPartners() throws Exception {
        // view & notification
        User user = em.find(User.class, 3);
        Database database = em.find(Database.class, 1);

        LocalDate today = new LocalDate(2041, 1, 1);
        UserDigest userDigest = new UserDigest(user, today.atMidnightInMyTimezone(), 1);
        ActivityDigestModel activityDigestModel = activityDigestModelBuilder.createModel(userDigest);
        SiteHistory lastEdit = activityDigestModelBuilder.findLastEdit(database);

        ActivityDigestModel.DatabaseModel databaseModel = new ActivityDigestModel.DatabaseModel(activityDigestModel, database, lastEdit);
        List<Partner> partners = activityDigestModelBuilder.findPartners(databaseModel);

        assertThat(partners.size(), is(equalTo(1)));
        assertTrue(partners.contains(em.find(Partner.class, 1)));
    }

}