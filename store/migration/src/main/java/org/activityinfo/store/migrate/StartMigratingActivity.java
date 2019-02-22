package org.activityinfo.store.migrate;

import com.google.appengine.tools.mapreduce.MapJob;
import com.google.appengine.tools.mapreduce.MapSpecification;
import com.google.appengine.tools.pipeline.Job0;
import com.google.appengine.tools.pipeline.Value;


/**
 * To migrate an activity to HRD, we need to follow a series of steps.
 *
 *
 */
public class StartMigratingActivity extends Job0<Void> {

    private int activityId;

    public StartMigratingActivity(int activityId) {
        this.activityId = activityId;
    }

    @Override
    public Value<Void> run() throws Exception {

        // First, we need to verify that HRD is consistent with what is stored
        // in MySQL.
        // Sources of inconsistency:
        //  - Sites created before dual-write implemented
        //  - Miswrites due to tx bugs

        SiteInput input = new SiteInput(activityId);
        SiteMigrator mapper = new SiteMigrator(false);

        MapSpecification<Integer, Void, Void> spec = new MapSpecification.Builder<Integer, Void, Void>(input, mapper)
                .setJobName("Migrate site records")
                .build();

        MapJob<Integer, Void, Void> mapJob = new MapJob<>(spec, MigrationServlet.getSettings());


        throw new UnsupportedOperationException("TODO");
    }
}
