package org.activityinfo.server.endpoint.odk;

import com.google.common.base.Optional;
import org.activityinfo.io.xform.formList.XFormList;
import org.activityinfo.io.xform.formList.XFormListItem;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.ActivityDTO;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.model.auth.AuthenticatedUser;
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
}
