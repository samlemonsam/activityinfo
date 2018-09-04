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
package org.activityinfo.server.generated;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import org.activityinfo.server.util.jaxrs.Domain;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 * GeneratedResource implementation backed by metadata stored in the AppEngine HRD
 * and data in Google Cloud storage
 */
class GcsGeneratedResource implements GeneratedResource {

    private static final Logger LOGGER = Logger.getLogger(GcsGeneratedResource.class.getName());
    
    private Domain domain;
    private String bucket;
    private GcsGeneratedMetadata metadata;

    GcsGeneratedResource(Domain domain, String bucket, GcsGeneratedMetadata metadata) {
        this.domain = domain;
        this.bucket = bucket;
        this.metadata = metadata;
    }

    @Override
    public String getId() {
        return metadata.getId();
    }

    private BlobKey getAppEngineBlobKey() {
        BlobstoreService blobstore = BlobstoreServiceFactory.getBlobstoreService();
        return blobstore.createGsBlobKey("/gs/" + bucket + "/" + metadata.getGcsPath());
    }

    @Override
    public boolean isComplete() {
        return metadata.isCompleted();
    }

    @Override
    public String getDownloadUri() {
        return UriBuilder.fromUri(domain.getRootUrl())
                .path("generated")
                .path(metadata.getId())
                .path(metadata.getFilename())
                .build()
                .toString();
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return new GcsOutputStream(bucket, metadata);
    }

    @Override
    public void updateProgress(double percentageComplete) {
        metadata.setPercentageComplete(percentageComplete);
        metadata.save();
    }

    @Override
    public double getProgress() {
        return metadata.getPercentageComplete();
    }

    @Override
    public Response serve() throws IOException {
        
        LOGGER.info(format("Serving generated resource %s: %s [%s]", 
                metadata.getId(), 
                metadata.getFilename(),
                metadata.getContentType()));

        // Tell the serving infrastructure to serve the file directly
        // from Google Cloud Storage.
        // See https://cloud.google.com/appengine/docs/standard/go/how-requests-are-handled

        return Response.ok()
                .header("Content-Type", metadata.getContentType())
                .header("Content-Disposition", "attachment")
                .header("X-AppEngine-BlobKey", getAppEngineBlobKey().getKeyString())
                .build();
    }

}
