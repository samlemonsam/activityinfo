package org.activityinfo.legacy.shared.adapter;

import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.server.command.CommandTestCase2;
import org.activityinfo.server.database.OnDataSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by yuriyz on 4/25/2016.
 */
@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/sites-simple1.db.xml")
public class HttpQueryExecutorTest extends CommandTestCase2 {

    private HttpQueryExecutor executor;

    @Before
    public final void setup() {
        executor = new HttpQueryExecutor();
    }

    @Test
    public void columns() {
        // TODO
        executor.query(new QueryModel());
    }
}
