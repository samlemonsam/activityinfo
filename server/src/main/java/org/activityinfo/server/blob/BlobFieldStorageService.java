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
package org.activityinfo.server.blob;

import com.sun.jersey.api.core.InjectParam;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.BlobId;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Provides storage for fields which have blob values, such as images
 * or general attachment fields.
 */

//@Path("/service/blob") commented path to avoid jersey conflict during binding in GcsBlobFieldStorageServiceModule
//Error : Conflicting URI templates. The URI template /service/blob for root resource class org.activityinfo.server.blob.GcsBlobFieldStorageService and the URI template /service/blob transform to the same regular expression /service/blob(/.*)?
public interface BlobFieldStorageService {

    /**
     * Provides a temporary, signed URL via which the user can access a blob
     * associated with a field value.
     *
     * @param blobId
     * @return blob uri
     */
    @GET
    @Path("{blobId}/{resourceId}/blobUrl")
    Response getBlobUrl(@InjectParam AuthenticatedUser user,
                        @PathParam("blobId") BlobId blobId,
                        @PathParam("resourceId") ResourceId resourceId);


    @GET
    @Path("{blobId}/{resourceId}/image")
    Response getImage(@InjectParam AuthenticatedUser user,
                      @PathParam("blobId") BlobId blobId,
                      @PathParam("resourceId") ResourceId resourceId) throws IOException;

    @GET
    @Path("{blobId}/{resourceId}/imageUrl")
    Response getImageUrl(@InjectParam AuthenticatedUser user,
                         @PathParam("blobId") BlobId blobId,
                         @PathParam("resourceId") ResourceId resourceId) throws IOException;

    @GET
    @Path("{blobId}/{resourceId}/thumbnail")
    Response getThumbnail(@InjectParam AuthenticatedUser user,
                          @PathParam("blobId") BlobId blobId,
                          @PathParam("resourceId") ResourceId resourceId,
                          @QueryParam("width") int width,
                          @QueryParam("height") int height);

    @GET
    @Path("{blobId}/{resourceId}/exists")
    Response exists(@InjectParam AuthenticatedUser user,
                    @PathParam("blobId") BlobId blobId,
                    @PathParam("resourceId") ResourceId resourceId);

    @POST
    @Path("credentials/{blobId}/{resourceId}/{fileName}")
    Response getUploadCredentials(@InjectParam AuthenticatedUser user,
                                  @PathParam("blobId") BlobId blobId,
                                  @PathParam("resourceId") ResourceId resourceId,
                                  @PathParam("fileName") String fileName);

}
