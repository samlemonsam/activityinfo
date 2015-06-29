package org.activityinfo.server.endpoint.odk;

import com.google.common.collect.Maps;
import com.sun.jersey.api.view.Viewable;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.ActivityDTO;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.server.util.monitoring.Timed;
legacy.shared.model.ActivityDTO;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.server.util.monitoring.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("/formList")
public class FormListResource extends ODKResource {


    @GET
    @Timed(name = "odk", kind = "formList")
    @Produces(MediaType.TEXT_XML)
    public Response formList(@Context UriInfo info) throws Exception {
        if (enforceAuthorization()) {
            return askAuthentication();
        }
        LOGGER.finer("ODK formlist requested by " + getUser().getEmail() + " (" + getUser().getId() + ")");

        List<ActivityDTO> activities = new ArrayList<>();
        SchemaDTO schema = dispatcher.execute(new GetSchema());

        for (UserDatabaseDTO database : schema.getDatabases()) {
            if(canBeSubmittedViaOdk(database)) {
                for (ActivityDTO activity : database.getActivities()) {
                    if (canBeSubmittedViaOdk(activity)) {
                        activities.add(activity);
                    }
                }
            }
        }

        Map<String, Object> map = Maps.newHashMap();
        map.put("activities", activities);
        map.put("host", info.getBaseUri().toString());

        return Response.ok(new Viewable("/odk/formList.ftl", map)).build();
    }

    private boolean canBeSubmittedViaOdk(UserDatabaseDTO database) {
        // Edit permissions required
        if(!database.isEditAllowed()) {
            return false;
        }
        // Forms cannot be submitted without a partner 
        if(database.getPartners().isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if the activity can be entered as an ODK form.
     */
    private boolean canBeSubmittedViaOdk(ActivityDTO activity) {

        // only "once" activities can be submitted via odk because
        // we don't have a good way of allowing users to choose 
        // the site or edit existing monthly reports
        if (activity.getReportingFrequency() == ActivityDTO.REPORT_MONTHLY) {
            return false;
        }

        // Activities bound to admin levels cannot be submitted via ODK
        // because of problems with cascading lists
        if (activity.getLocationType().isAdminLevel()) {
            return false;
        }

        return true;
    }
}
