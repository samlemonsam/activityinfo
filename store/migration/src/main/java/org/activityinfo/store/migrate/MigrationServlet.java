package org.activityinfo.store.migrate;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.mapreduce.MapJob;
import com.google.appengine.tools.mapreduce.MapSettings;
import com.google.appengine.tools.mapreduce.MapSpecification;
import com.google.appengine.tools.mapreduce.inputs.DatastoreInput;
import com.google.appengine.tools.pipeline.PipelineService;
import com.google.appengine.tools.pipeline.PipelineServiceFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Starts Map/Reduce jobs
 */
public class MigrationServlet extends HttpServlet {

    private final PipelineService pipelineService = PipelineServiceFactory.newPipelineService();


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String pipelineId;
        try {
            pipelineId = startJob(resp, req.getParameter("job"));
        } catch (IllegalArgumentException e) {
            resp.setStatus(400);
            resp.getOutputStream().println(e.getMessage());
            return;
        }

        resp.sendRedirect("/_ah/pipeline/status.html?root=" + pipelineId);
    }

    private String startJob(HttpServletResponse resp, String job) {
        if(job == null) {
            throw new IllegalArgumentException("missing job parameter");
        }
        switch (job) {
            case "partners":
                return startPartnerMigration();
            case "snapshots":
                return reindexSnapshots();
            default:
                throw new IllegalArgumentException("Unknown job: " + job);
        }
    }

    private String reindexSnapshots() {
        DatastoreInput input = new DatastoreInput("FormRecordSnapshot", 10);
        SnapshotReindexer reindexer = new SnapshotReindexer();

        MapSpecification<Entity, Void, Void> spec = new MapSpecification.Builder<Entity, Void, Void>(input, reindexer)
                .setJobName("Reindex snapshot indexes")
                .build();

        return MapJob.start(spec, getSettings());
    }

    private String startPartnerMigration() {
        DatabaseInput input = new DatabaseInput();
        ProjectMigrator mapper = new ProjectMigrator();

        MapSpecification<Integer, Void, Void> spec = new MapSpecification.Builder<Integer, Void, Void>(input, mapper)
                .setJobName("Migrate MapReduce entities")
                .build();

        return MapJob.start(spec, getSettings());

    }


    private MapSettings getSettings() {
        MapSettings settings = new MapSettings.Builder()
                .setWorkerQueueName("default")
                .setModule("migration")
                .build();
        return settings;
    }
}
