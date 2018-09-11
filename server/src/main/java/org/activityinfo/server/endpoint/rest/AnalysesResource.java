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
import org.activityinfo.server.command.handler.LegacyPermissionAdapter;
import org.activityinfo.store.hrd.Hrd;
import org.activityinfo.store.hrd.entity.AnalysisEntity;
import org.activityinfo.store.hrd.entity.AnalysisSnapshotEntity;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Date;

import static org.activityinfo.json.Json.parse;

public class AnalysesResource {

    private final LegacyPermissionAdapter legacyPermissionAdapter;
    private final Provider<AuthenticatedUser> userProvider;

    public AnalysesResource(LegacyPermissionAdapter legacyPermissionAdapter, Provider<AuthenticatedUser> userProvider) {
        this.legacyPermissionAdapter = legacyPermissionAdapter;
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
        return legacyPermissionAdapter.isViewAllowed(databaseId, userProvider.get());
    }

    private void assertAuthorized(AnalysisUpdate update) {
        ResourceId databaseId = ResourceId.valueOf(update.getParentId());
        if(databaseId.getDomain() != CuidAdapter.DATABASE_DOMAIN) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("No such folder: " + databaseId).build());
        }

        legacyPermissionAdapter.assertDesignPrivileges(CuidAdapter.getLegacyIdFromCuid(databaseId), userProvider.get());
    }



}
