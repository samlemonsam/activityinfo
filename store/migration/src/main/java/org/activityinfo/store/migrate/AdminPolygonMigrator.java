package org.activityinfo.store.migrate;

import com.google.appengine.tools.cloudstorage.*;
import com.google.common.io.ByteStreams;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminPolygonMigrator extends MigratingMapper {

    public static final String QUERY = "select adminentityid, adminlevelid, geometry from adminentity " +
            "where geometry is not null " +
            "and deleted = 0 " +
            "and adminentityid > ? " +
            "order by adminentityid";

    @Override
    protected void execute(ResultSet rs) throws SQLException {

        int adminEntityId = rs.getInt(1);
        int adminLevelId = rs.getInt(2);

        ResourceId formId = CuidAdapter.adminLevelFormClass(adminLevelId);
        ResourceId recordId = CuidAdapter.entity(adminEntityId);
        ResourceId fieldId = CuidAdapter.field(formId, CuidAdapter.GEOMETRY_FIELD);

        GcsService gcsService = GcsServiceFactory.createGcsService();

        GcsFilename gcsFilename = new GcsFilename("activityinfoeu-geometry",
                String.format("%s/%s/%s.1",
                        formId.asString(),
                        fieldId.asString(),
                        recordId.asString()));

        if(!exists(gcsService, gcsFilename)) {

            GcsFileOptions options = new GcsFileOptions.Builder()
                    .mimeType("application/octet-stream")
                    .build();

            try (GcsOutputChannel outputChannel = gcsService.createOrReplace(gcsFilename, options)) {
                try (OutputStream outputStream = Channels.newOutputStream(outputChannel)) {
                    try (InputStream inputStream = rs.getBinaryStream(3)) {
                        ByteStreams.copy(inputStream, outputStream);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean exists(GcsService gcsService, GcsFilename gcsFilename) {
        try {
            GcsFileMetadata metadata = gcsService.getMetadata(gcsFilename);
            return metadata.getLength() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
