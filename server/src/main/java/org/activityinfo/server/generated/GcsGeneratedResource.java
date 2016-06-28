package org.activityinfo.server.generated;

import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.common.io.ByteStreams;
import com.google.inject.Provider;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
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
    
    private String bucket;
    private GcsGeneratedMetadata metadata;
    private Provider<HttpServletRequest> request;
    
    GcsGeneratedResource(String bucket, GcsGeneratedMetadata metadata,
                         Provider<HttpServletRequest> request) {
        this.bucket = bucket;
        this.metadata = metadata;
        this.request = request;
    }

    @Override
    public String getId() {
        return metadata.getId();
    }

    @Override
    public boolean isComplete() {
        return metadata.isCompleted();
    }

    @Override
    public String getDownloadUri() {
        StringBuilder url = new StringBuilder();
        if(request.get().isSecure()) {
            url.append("https://");
        } else {
            url.append("http://");
        } 
        url.append(request.get().getServerName());
        
        int defaultPort = request.get().isSecure() ? 443 : 80;
        int port = request.get().getServerPort();
        
        if(port != defaultPort) {
            url.append(":").append(port);
        }
        return url
        .append("/generated/")
        .append(metadata.getId())
        .append("/")
        .append(metadata.getFilename())
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
            return serveContent();
    }

    public Response serveContent() throws IOException {
        
        LOGGER.info(format("Serving generated resource %s: %s [%s]", 
                metadata.getId(), 
                metadata.getFilename(),
                metadata.getContentType()));
        
        return Response.ok(new Download(), metadata.getContentType())
                .header("Content-disposition", "attachment")
                .build();
    }

    private class Download implements StreamingOutput {

        @Override
        public void write(OutputStream outputStream) throws IOException, WebApplicationException {
            GcsService gcs = GcsServiceFactory.createGcsService();
            GcsFilename fileName = new GcsFilename(bucket, metadata.getGcsPath());
            long startPosition = 0;
            GcsInputChannel readChannel = gcs.openReadChannel(fileName, startPosition);
            try(InputStream inputStream = Channels.newInputStream(readChannel)) {
                ByteStreams.copy(inputStream, outputStream);
            }
        }
    }
}
