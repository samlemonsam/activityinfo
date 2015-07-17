package org.activityinfo.model.expr.customer;

import com.google.common.base.Joiner;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.PivotSites;
import org.activityinfo.legacy.shared.command.result.Bucket;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.server.command.CommandTestCase2;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(InjectionSupport.class)
public class LiveTest extends CommandTestCase2 {


    @Test
    public void testLive() {
        int indicatorId = 37242;
        int userId = 2195;

        setUser(userId);

        Filter filter = new Filter();
        filter.addRestriction(DimensionType.Indicator, indicatorId);

        PivotSites pivot = new PivotSites();
        pivot.setDimensions(new Dimension(DimensionType.Indicator));
        pivot.setFilter(filter);

        List<Bucket> buckets = execute(pivot).getBuckets();
        System.out.println(Joiner.on("\n").join(buckets));

    }

}

