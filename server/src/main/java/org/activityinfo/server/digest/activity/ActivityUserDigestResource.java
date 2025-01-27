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
package org.activityinfo.server.digest.activity;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.server.authentication.ServerSideAuthProvider;
import org.activityinfo.server.digest.UserDigestResource;
import org.activityinfo.server.mail.MailSender;

import javax.persistence.EntityManager;
import javax.ws.rs.Path;

@Path(ActivityUserDigestResource.ENDPOINT)
public class ActivityUserDigestResource extends UserDigestResource {
    public static final String ENDPOINT = "/tasks/activityuserdigest";
    public static final int PARAM_DAYS_DEF = 7; // one week

    @Inject
    public ActivityUserDigestResource(Provider<EntityManager> entityManager,
                                      Provider<MailSender> mailSender,
                                      ServerSideAuthProvider authProvider,
                                      ActivityDigestModelBuilder activityDigestModelBuilder,
                                      ActivityDigestRenderer activityDigestRenderer) {
        super(entityManager, mailSender, authProvider, activityDigestModelBuilder, activityDigestRenderer);
    }

    @Override
    public int getDefaultDays() {
        return PARAM_DAYS_DEF;
    }
}
