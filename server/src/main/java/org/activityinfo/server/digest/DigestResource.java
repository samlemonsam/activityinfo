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
package org.activityinfo.server.digest;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.logging.Logger;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

public abstract class DigestResource {
    public static final String USERDIGEST_QUEUE = "userdigest";

    private static final Logger LOGGER = Logger.getLogger(DigestResource.class.getName());

    @GET 
    @Produces(MediaType.TEXT_PLAIN)
    public String createDigests() throws Exception {

        List<Integer> userIds = selectUsers();

        String msg = "creating digests for " + userIds.size() + " users";
        LOGGER.info(msg);

        Queue queue = QueueFactory.getQueue(USERDIGEST_QUEUE);

        for (Integer userId : userIds) {
            TaskOptions taskoptions = withUrl(getUserDigestEndpoint())
                    .param(UserDigestResource.PARAM_USER, String.valueOf(userId))
                    .method(Method.GET);
            queue.add(taskoptions);
        }
        return msg;
    }

    public abstract List<Integer> selectUsers();

    public abstract String getUserDigestEndpoint();

}
