package org.activityinfo.server.generated;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.common.io.ByteStreams;
import org.activityinfo.server.database.hibernate.entity.Domain;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
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
        return blobstore.createGsBlobKey("/gs/" + bucket + "/" + metadata.getId());
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
        // The use of the X-AppEngine-BlobKey header is undocumented,
        // but reverse-engineered from
        // com.google.appengine.api.blobstore.BlobstoreServiceImpl.serve()
        // which unfortunately assumes that it is being called from a Servlet implementation.

        return Response.ok()
                .header("Content-Type", metadata.getContentType())
                .header("Content-Disposition", "attachment")
                .header("X-AppEngine-BlobKey", getAppEngineBlobKey().getKeyString())
                .build();
    }

}
