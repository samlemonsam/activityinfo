package org.activityinfo.service.blob;

import com.google.api.client.util.Preconditions;
import com.google.api.client.util.Strings;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.tools.cloudstorage.GcsFileMetadata;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.activityinfo.model.auth.AuthenticatedUser;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.attachment.AppEngineAttachmentService;
import org.activityinfo.server.authentication.ServerSideAuthProvider;
import org.activityinfo.store.spi.BlobId;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by yuriyz on 6/22/2016.
 */
@Singleton
public class GcsBlobServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(GcsBlobServlet.class.getName());

    private final GcsBlobFieldStorageService service;
    private final ServerSideAuthProvider authProvider;

    @Inject
    public GcsBlobServlet(GcsBlobFieldStorageService service, ServerSideAuthProvider authProvider) {
        this.service = service;
        this.authProvider = authProvider;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            BlobId blobId = new BlobId(request.getParameter("blobId"));
            ResourceId resourceId = ResourceId.valueOf(request.getParameter("resourceId"));

            Preconditions.checkState(!Strings.isNullOrEmpty(blobId.asString()));
            Preconditions.checkState(!Strings.isNullOrEmpty(resourceId.asString()));

            AuthenticatedUser user = authProvider.get();
            service.assertNotAnonymousUser(user);
            service.assertHasAccess(user, blobId, resourceId);
            service.assertBlobExists(blobId);

            GcsFileMetadata metadata = GcsServiceFactory.createGcsService().getMetadata(new GcsFilename(service.getBucketName(), blobId.asString()));

            response.setHeader("Content-Disposition", metadata.getOptions().getContentDisposition());
            response.setContentType(metadata.getOptions().getMimeType());

            BlobstoreServiceFactory.getBlobstoreService().serve(service.blobKey(blobId), response);
        } catch (WebApplicationException e) {
            sendError(response, e);
        }
    }

    private void sendError(HttpServletResponse response, WebApplicationException e) throws IOException {
        LOGGER.log(Level.FINE, e.getMessage(), e);
        response.sendError(e.getResponse().getStatus(), e.getResponse().getEntity() != null ? e.getResponse().getEntity().toString() : "");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            List<FileItem> fileItems = getFileItems(request);

            BlobId blobId = new BlobId(getFileItems(fileItems, "blobId").getString());
            String mimeType = getFileItems(fileItems, "mimeType").getString();
            String fileName = getFileItems(fileItems, "fileName").getString();
            ResourceId resourceId = ResourceId.valueOf(getFileItems(fileItems, "resourceId").getString());

            FileItem fileItem = getFileItems(fileItems, "file");

            Preconditions.checkState(!Strings.isNullOrEmpty(blobId.asString()));
            Preconditions.checkState(!Strings.isNullOrEmpty(resourceId.asString()));

            service.put(authProvider.get(), "attachment; filename=\"" + fileName + "\"",
                    mimeType, blobId, resourceId, fileItem.getInputStream());
        } catch (WebApplicationException e) {
            sendError(response, e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error handling upload", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private FileItem getFileItems(List<FileItem> items, String name) {
        for (FileItem item : items) {
            if (item.getFieldName().equalsIgnoreCase(name)) {
                return item;
            }
        }
        throw new RuntimeException("Failed to find file item with name: " + name);
    }

    private List<FileItem> getFileItems(HttpServletRequest request) throws FileUploadException {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart) {

            FileItemFactory factory = new FileItemFactory() {
                @Override
                public FileItem createItem(String fieldName, String contentType, boolean isFormField, String fileName) {
                    return new AppEngineAttachmentService.InMemoryFileItem(fieldName, contentType, isFormField, fileName);
                }
            };
            ServletFileUpload upload = new ServletFileUpload(factory);

            return upload.parseRequest(request);
        }
        throw new RuntimeException("No upload provided");
    }
}
