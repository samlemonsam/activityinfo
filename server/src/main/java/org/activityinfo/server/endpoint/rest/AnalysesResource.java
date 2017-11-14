package org.activityinfo.server.endpoint.rest;

import com.google.inject.Provider;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.googlecode.objectify.Work;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonMappingException;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.model.analysis.Analysis;
import org.activityinfo.model.analysis.AnalysisUpdate;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.command.handler.PermissionOracle;
import org.activityinfo.store.hrd.Hrd;
import org.activityinfo.store.hrd.entity.AnalysisEntity;
import org.activityinfo.store.hrd.entity.AnalysisSnapshotEntity;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Date;

import static org.activityinfo.json.Json.parse;

public class AnalysesResource {

    private final PermissionOracle permissionOracle;
    private final Provider<AuthenticatedUser> userProvider;

    public AnalysesResource(PermissionOracle permissionOracle, Provider<AuthenticatedUser> userProvider) {
        this.permissionOracle = permissionOracle;
        this.userProvider = userProvider;
    }

    @GET
    @Path("{id}")
    public Response get(@PathParam("id") final String id) {
        return Hrd.ofy().transact(new Work<Response>() {
            @Override
            public Response run() {
                AnalysisEntity entity = Hrd.ofy().load().key(Key.create(AnalysisEntity.class, id)).now();
                if(entity == null) {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }

                if(!isVisible(entity)) {
                    return Response.status(Response.Status.FORBIDDEN).build();
                }

                Analysis analysis = new Analysis();
                analysis.setId(id);
                analysis.setParentId(entity.getParentId());
                analysis.setLabel(entity.getLabel());
                analysis.setModelType(entity.getType());
                analysis.setModel(parse(entity.getModel()));

                return Response.ok().entity(Json.toJson(analysis).toJson()).build();
            }
        });
    }


    @POST
    public Response update(String jsonString) throws JsonMappingException {

        final AnalysisUpdate update = Json.fromJson(AnalysisUpdate.class, Json.parse(jsonString));

        assertAuthorized(update);

        // TODO: verify json
        Hrd.ofy().transact(new Runnable() {
            @Override
            public void run() {

                LoadResult<AnalysisEntity> existingEntity = Hrd.ofy().load().key(Key.create(AnalysisEntity.class, update.getId()));
                long newVersion;
                if(existingEntity.now() == null) {
                    newVersion = 1;
                } else {
                    newVersion = existingEntity.now().getVersion() + 1;
                }

                AnalysisEntity entity = new AnalysisEntity();
                entity.setId(update.getId());
                entity.setParentId(update.getParentId());
                entity.setType(update.getType());
                entity.setVersion(newVersion);
                entity.setLabel(update.getLabel());
                entity.setModel(update.getModel().toJson());

                AnalysisSnapshotEntity snapshot = new AnalysisSnapshotEntity();
                snapshot.setAnalysis(Key.create(entity));
                snapshot.setVersion(newVersion);
                snapshot.setType(update.getType());
                snapshot.setLabel(update.getLabel());
                snapshot.setModel(update.getModel().toJson());
                snapshot.setTime(new Date());
                snapshot.setUserId(userProvider.get().getId());

                Hrd.ofy().save().entities(entity, snapshot);
            }
        });

        return Response.status(Response.Status.OK).build();
    }


    private boolean isVisible(AnalysisEntity entity) {
        ResourceId databaseId = ResourceId.valueOf(entity.getParentId());
        return permissionOracle.isViewAllowed(databaseId, userProvider.get());
    }

    private void assertAuthorized(AnalysisUpdate update) {
        ResourceId databaseId = ResourceId.valueOf(update.getParentId());
        if(databaseId.getDomain() != CuidAdapter.DATABASE_DOMAIN) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("No such folder: " + databaseId).build());
        }

        permissionOracle.assertDesignPrivileges(CuidAdapter.getLegacyIdFromCuid(databaseId), userProvider.get());
    }



}
