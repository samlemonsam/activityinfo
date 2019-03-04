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
package org.activityinfo.server.endpoint.odk;

import com.google.common.base.Optional;
import org.activityinfo.io.xform.formList.XFormList;
import org.activityinfo.io.xform.formList.XFormListItem;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.ActivityDTO;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.server.command.DispatcherSync;

import javax.inject.Provider;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.logging.Logger;

/**
 * Created by yuriyz on 6/16/2016.
 */
public class FormLister {

    private static final Logger LOGGER = Logger.getLogger(FormLister.class.getName());

    private Provider<AuthenticatedUser> authProvider;
    private DispatcherSync dispatcher;

    public FormLister(Provider<AuthenticatedUser> authProvider, DispatcherSync dispatcher) {
        this.authProvider = authProvider;
        this.dispatcher = dispatcher;
    }

    public Response formList(UriInfo uri, Optional<Integer> dbIdFilter) throws Exception {
        AuthenticatedUser user = authProvider.get();

        LOGGER.finer("ODK form list requested by " + user.getEmail() + " (" + user.getId() + ")");

        SchemaDTO schema = dispatcher.execute(new GetSchema());

        XFormList formList = new XFormList();
        for (UserDatabaseDTO db : schema.getDatabases()) {
            if (dbIdFilter.isPresent() && db.getId() != dbIdFilter.get()) {
                continue; // skip
            }
            if (db.isEditAllowed()) {
                for (ActivityDTO activity : db.getActivities()) {
                    if(hasAdminLevelLocation(activity)) {
                        // Admin Level Locations are invalid for ODK forms - do not show
                        continue;
                    }
                    XFormListItem form = new XFormListItem();
                    form.setName(db.getName() + " / " + activity.getName());
                    form.setFormId("activityinfo.org:" + activity.getId());
                    form.setVersion(getVersion());

                    form.setDownloadUrl(uri.getBaseUriBuilder()
                            .path(XFormResources.class)
                            .path(Integer.toString(activity.getId()))
                            .path("xform")
                            .build());

                    formList.getItems().add(form);
                }
            }
        }
        return OpenRosaResponse.build(formList);
    }

    private String getVersion() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }

    private boolean hasAdminLevelLocation(ActivityDTO activity) {
        return activity.getLocationType().isAdminLevel();
    }
}
