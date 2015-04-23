package org.activityinfo.server.endpoint.odk;

import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.ActivityDTO;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.model.auth.AuthenticatedUser;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.io.xform.formList.XFormList;
import org.activityinfo.io.xform.formList.XFormListItem;

import javax.inject.Provider;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.logging.Logger;

@Path("/formList")
public class FormListResource {
    private static final Logger LOGGER = Logger.getLogger(FormListResource.class.getName());



    private Provider<AuthenticatedUser> authProvider;
    private DispatcherSync dispatcher;

    @Inject
    public FormListResource(OdkAuthProvider authProvider, DispatcherSync dispatcher) {
        this.authProvider = authProvider;
        this.dispatcher = dispatcher;
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    public Response formList(@Context UriInfo uri) throws Exception {
        AuthenticatedUser user = authProvider.get();

        LOGGER.finer("ODK form list requested by " + user.getEmail() + " (" + user.getId() + ")");

        SchemaDTO schema = dispatcher.execute(new GetSchema());

        XFormList formList = new XFormList();
        for (UserDatabaseDTO db : schema.getDatabases()) {
            if (db.isEditAllowed()) {
                for (ActivityDTO activity : db.getActivities()) {
                    XFormListItem form = new XFormListItem();
                    form.setName(db.getName() + " / " + activity.getName());
                    form.setFormId("activityinfo.org:" + activity.getId());
                    form.setVersion(getVersion());

                    form.setDownloadUrl(uri.getBaseUriBuilder()
                            .path(FormResource.class)
                            .path(Integer.toString(activity.getId()))
                            .path("xform")
                            .build());

                    // skip itemset for LCCA form: earlier versions triggered
                    // a bug in ODK and have corrupted the sqlite database on those
                    // devices.
                    if(activity.getId() != 11218) {
                        form.setManifestUrl(uri.getBaseUriBuilder()
                                .path(FormResource.class)
                                .path(Integer.toString(activity.getId()))
                                .path("manifest")
                                .build());
                    }

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