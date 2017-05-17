package org.activityinfo.server.job;

import com.google.inject.Inject;
import com.googlecode.objectify.VoidWork;
import net.lightoze.gwt.i18n.server.ThreadLocalLocaleProvider;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.model.job.JobDescriptor;
import org.activityinfo.model.job.JobResult;
import org.activityinfo.model.job.JobState;
import org.activityinfo.server.authentication.ServerSideAuthProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Handles requests from the Task-queue service
 */
public class JobTaskServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(JobTaskServlet.class.getName());

    private ServerSideAuthProvider authProvider;
    private ExecutorFactory executorFactory;

    @Inject
    public JobTaskServlet(ServerSideAuthProvider authProvider, ExecutorFactory executorFactory) {
        this.authProvider = authProvider;
        this.executorFactory = executorFactory;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String jobKey = req.getParameter(JobResource.JOB_KEY_PARAM);
        final JobEntity jobEntity = JobStore.getUserJob(jobKey).now();
        if(jobEntity == null) {
            LOGGER.severe("UserJob with key " + jobKey + " not found");
            resp.setStatus(200);
            return;
        }

        if(jobEntity.getState() != JobState.STARTED) {
            LOGGER.info("UserJob " + jobKey + " already complete.");
        }

        authProvider.set(new AuthenticatedUser("",
                Integer.parseInt(req.getParameter("userId")),
                req.getParameter("userEmail")));

        ThreadLocalLocaleProvider.pushLocale(Locale.forLanguageTag(jobEntity.getLocale()));

        JobExecutor executor = executorFactory.create(jobEntity.getType());
        JobDescriptor descriptor = jobEntity.parseDescriptor();

        try {
            final JobResult result = executor.execute(descriptor);
            markCompleted(jobEntity, result);
        } catch (Exception e) {
            markFailed(jobEntity, e);
        }
    }

    private void markCompleted(final JobEntity jobEntity, final JobResult result) {
        JobStore.ofy().transact(new Runnable() {
            @Override
            public void run() {
                JobEntity updatedEntity = JobStore.getUserJob(JobStore.getWebSafeKeyString(jobEntity)).now();
                if(updatedEntity.getState() != JobState.STARTED) {
                    return;
                }
                updatedEntity.setResult(result.toJsonObject().toString());
                updatedEntity.setState(JobState.COMPLETED);
                updatedEntity.setCompletionTime(new Date());
                JobStore.ofy().save().entity(updatedEntity).now();
            }
        });
    }


    private void markFailed(final JobEntity jobEntity, Exception e) {
        JobStore.ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                JobEntity updatedEntity = JobStore.getUserJob(JobStore.getWebSafeKeyString(jobEntity)).now();
                if(updatedEntity.getState() != JobState.STARTED) {
                    return;
                }
                updatedEntity.setState(JobState.FAILED);
                JobStore.ofy().save().entity(updatedEntity).now();
            }
        });
    }

}
