/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.server.job;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.googlecode.objectify.VoidWork;
import net.lightoze.gwt.i18n.server.ThreadLocalLocaleProvider;
import org.activityinfo.model.job.JobDescriptor;
import org.activityinfo.model.job.JobResult;
import org.activityinfo.model.job.JobState;
import org.activityinfo.server.authentication.ServerSideAuthProvider;
import org.activityinfo.server.database.hibernate.entity.User;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles requests from the Task-queue service
 */
@Singleton
public class JobTaskServlet extends HttpServlet {

    public static final String END_POINT = "/tasks/job";

    private static final Logger LOGGER = Logger.getLogger(JobTaskServlet.class.getName());

    private ServerSideAuthProvider authProvider;
    private ExecutorFactory executorFactory;
    private Provider<EntityManager> entityManager;

    @Inject
    public JobTaskServlet(ServerSideAuthProvider authProvider, ExecutorFactory executorFactory,
                          Provider<EntityManager> entityManager) {
        this.authProvider = authProvider;
        this.executorFactory = executorFactory;
        this.entityManager = entityManager;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if(Strings.isNullOrEmpty(req.getHeader("X-AppEngine-QueueName"))) {
            LOGGER.severe("Unauthorized task request");
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String jobKey = req.getParameter(JobResource.JOB_KEY_PARAM);
        final JobEntity jobEntity = JobStore.getUserJob(jobKey).now();
        if(jobEntity == null) {
            LOGGER.severe("Job " + jobKey + " not found");
            resp.setStatus(200);
            return;
        }

        if(jobEntity.getState() != JobState.STARTED) {
            LOGGER.info("Job " + jobKey + " has state " + jobEntity.getState() + ", stopping here.");
            resp.setStatus(200);
            return;
        }

        User user = entityManager.get().find(User.class, (int)jobEntity.getUserId());
        authProvider.set(user.asAuthenticatedUser());

        Locale jobLocale = jobLocale(user, jobEntity);
        ThreadLocalLocaleProvider.pushLocale(jobLocale);

        LOGGER.info("Set authenticated user to " + user.getEmail() + " [" + jobLocale + "]");

        JobExecutor executor = executorFactory.create(jobEntity.getType());
        JobDescriptor descriptor = jobEntity.parseDescriptor();

        try {
            final JobResult result = executor.execute(descriptor);
            markCompleted(jobEntity, result);
        } catch (Exception e) {
            markFailed(jobEntity, e);
        }
    }

    private Locale jobLocale(User user, JobEntity jobEntity) {
        // if the locale for the job is specified explicitly, use that
        if(!Strings.isNullOrEmpty(jobEntity.getLocale())) {
            return Locale.forLanguageTag(jobEntity.getLocale());
        }
        // Otherwise use the user's default locale
        return user.getLocaleObject();
    }

    private void markCompleted(final JobEntity jobEntity, final JobResult result) {
        JobStore.ofy().transact(new Runnable() {
            @Override
            public void run() {
                JobEntity updatedEntity = JobStore.getUserJob(JobStore.getWebSafeKeyString(jobEntity)).now();
                if(updatedEntity.getState() != JobState.STARTED) {
                    return;
                }
                updatedEntity.setResult(result.toJson().toJson());
                updatedEntity.setState(JobState.COMPLETED);
                updatedEntity.setCompletionTime(new Date());
                JobStore.ofy().save().entity(updatedEntity).now();
            }
        });
    }


    private void markFailed(final JobEntity jobEntity, Exception e) {

        LOGGER.log(Level.SEVERE, "Job " + jobEntity.getType() + " failed: " + e.getMessage(), e);

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
