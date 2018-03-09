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

import com.google.appengine.api.datastore.*;
import com.google.common.base.Objects;
import com.google.inject.Inject;
import org.activityinfo.server.DeploymentConfiguration;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class TileResource {

    private static final String IMAGE_PROPERTY = "i";

    private final String authToken;
    private final DatastoreService datastore;

    @Inject
    public TileResource(DeploymentConfiguration config) {
        authToken = config.getProperty("tile.update.key");
        datastore = DatastoreServiceFactory.getDatastoreService();
    }

    @PUT 
    @Path("{layer}/{z}/{x}/{y}.png")
    public Response putTile(@HeaderParam("X-Update-Key") String authToken,
                            @PathParam("layer") String layer,
                            @PathParam("z") int zoom,
                            @PathParam("x") int x,
                            @PathParam("y") int y,
                            byte[] image) {

        if (!Objects.equal(this.authToken, authToken)) {
            throw new WebApplicationException(Status.UNAUTHORIZED);
        }

        Entity entity = new Entity(getKey(layer, zoom, x, y));
        entity.setProperty("i", new Blob(image));

        datastore.put(entity);

        return Response.ok().build();
    }

    @GET
    @Path("{layer}/{z}/{x}/{y}.png") 
    @Produces("image/png")
    public Response getTile(@PathParam("layer") String layer,
                            @PathParam("z") int zoom,
                            @PathParam("x") int x,
                            @PathParam("y") int y) {

        Entity entity;
        try {
            entity = datastore.get(getKey(layer, zoom, x, y));
        } catch (EntityNotFoundException e) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        Blob blob = (Blob) entity.getProperty(IMAGE_PROPERTY);
        return Response.ok(blob.getBytes()).build();
    }


    private Key getKey(String layer, int z, int x, int y) {
        return KeyFactory.createKey("Tile", layer + "_" + z + "_" + x + "_" + y);
    }

}
