package org.activityinfo.server.command.handler.sync;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.base.Stopwatch;
import com.google.inject.Inject;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.fixtures.MockHibernateModule;
import org.activityinfo.fixtures.Modules;
import org.activityinfo.legacy.shared.command.GetSyncRegionUpdates;
import org.activityinfo.legacy.shared.command.result.SyncRegionUpdate;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.endpoint.gwtrpc.GwtRpcModule;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import java.util.concurrent.TimeUnit;

/**
 * @author yuriyz on 01/22/2015.
 */
@RunWith(InjectionSupport.class)
@Modules({
        MockHibernateModule.class,
        GwtRpcModule.class,
})
@OnDataSet("/dbunit/schema2.db.xml")
public class DbUpdateBuilderTest {

    private static final int USER_ID = 1;

    @Inject
    protected EntityManager em;

    
    @Test
    public void optimizationTest() throws JSONException {

//        HibernateEntityManagerFactory hibernateFactory = (HibernateEntityManagerFactory) em;
//        Statistics statistics = hibernateFactory.getSessionFactory().getStatistics();
//        statistics.setStatisticsEnabled(true);

        User user = em.find(User.class, USER_ID);

        GetSyncRegionUpdates request = new GetSyncRegionUpdates();
        request.setRegionId("db/1");

        Stopwatch stopwatch = Stopwatch.createStarted();
        DbUpdateBuilder b = new DbUpdateBuilder(this.em);
        SyncRegionUpdate build = b.build(user, request);
        System.out.println(stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms, sql: " + build.getSql());

//        System.out.println("Number of queries: " + statistics.getQueries().length + " (was: 59)");
//        for (String q : statistics.getQueries()) {
//            System.out.println("   query: " + q);
//        }
//        System.out.println(statistics.getConnectCount());
//        System.out.println(statistics.getEntityFetchCount());
//        System.out.println(statistics.getSessionOpenCount());

    }
}
