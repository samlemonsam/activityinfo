package org.activityinfo.server.endpoint.odk;

import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFileOptions.Builder;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.ByteSource;
import com.google.inject.Inject;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.DeploymentConfiguration;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Archives the original XForm instances for debugging and
 * data recovery in the event of problems in translation from XForm to our internal
 * model.
 */
public class SubmissionArchiver {

    public static final String GCS_BUCKET_NAME_PROPERTY = "odk.backup.gcs.bucket.name";

    private static final Logger LOGGER = Logger.getLogger(SubmissionArchiver.class.getName());

    private final String bucketName;
    private final DateTimeFormatter dateFormat;
    private final DateTimeFormatter timeFormat;

    @Inject
    public SubmissionArchiver(DeploymentConfiguration config) {
        this.bucketName = config.getProperty(SubmissionArchiver.GCS_BUCKET_NAME_PROPERTY);
        this.dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd");
        this.timeFormat = DateTimeFormat.forPattern("HH:mm:ss.SSS");
    }

    public void backup(ResourceId formClassId, ResourceId formId, ByteSource byteSource) {
        try {

            DateTime submissionTime = new DateTime(DateTimeZone.UTC);

            String path = Joiner.on('/').join(
                    formClassId,
                    dateFormat.print(submissionTime),
                    timeFormat.print(submissionTime) + " " + formId + ".xml");

            GcsFilename gcsFilename = new GcsFilename(bucketName, path);
            GcsService gcsService = GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());

            GcsFileOptions gcsFileOptions = new Builder().mimeType(MediaType.MULTIPART_FORM_DATA).build();
            GcsOutputChannel channel = gcsService.createOrReplace(gcsFilename, gcsFileOptions);

            try (OutputStream outputStream = Channels.newOutputStream(channel)) {
                byteSource.copyTo(outputStream);
            }

            LOGGER.log(Level.INFO, "Archived XForm to " + gcsFilename);


        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Form submission could not be backed up to GCS", e);
            try {
                LOGGER.info(byteSource.asCharSource(Charsets.UTF_8).read());
            } catch (IOException ioException) {
            }
        }
    }
}
