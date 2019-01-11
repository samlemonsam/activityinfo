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
package org.activityinfo.server.attachment;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import org.activityinfo.server.DeploymentConfiguration;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.fileupload.util.FileItemHeadersImpl;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.channels.Channels;
import java.nio.charset.Charset;

public class AppEngineAttachmentService implements AttachmentService {

    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    private AppIdentityService identityService = AppIdentityServiceFactory.getAppIdentityService();

    private final String attachmentBucket;
    private final String attachmentPrefix;
    
    @Inject
    public AppEngineAttachmentService(DeploymentConfiguration config) {
        this.attachmentBucket = config.getProperty("attachment.bucket", identityService.getDefaultGcsBucketName());
        this.attachmentPrefix = config.getProperty("attachment.prefix", "attachments/");
    }
    
    @Override
    public void serveAttachment(String blobId, HttpServletResponse response) throws IOException {
        BlobKey blobKey = blobKey(blobId);
        blobstoreService.serve(blobKey, response);
    }
    
    private String gcsKey(String blobId) {
        return attachmentPrefix + blobId;
    }

    private BlobKey blobKey(String blobId) {
        return blobstoreService.createGsBlobKey("/gs/" + attachmentBucket + "/" + gcsKey(blobId));
    }

    @Override
    public void upload(String key, FileItem fileItem, InputStream uploadingStream) {
        try {
            GcsService gcsService = GcsServiceFactory.createGcsService();
            GcsFilename gcsFilename = new GcsFilename(attachmentBucket, gcsKey(key));
            GcsFileOptions options = new GcsFileOptions.Builder()
                    .contentDisposition("attachment; filename=\"" + fileItem.getName() + "\"")
                    .mimeType(fileItem.getContentType())
                    .build();
            
            try(OutputStream output = Channels.newOutputStream(gcsService.createOrReplace(gcsFilename, options))) {
                ByteStreams.copy(fileItem.getInputStream(), output);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String key) {
        blobstoreService.delete(blobKey(key));
    }

    @Override
    public FileItemFactory createFileItemFactory() {
        return new FileItemFactory() {

            @Override
            public FileItem createItem(String fieldName, String contentType, boolean isFormField, String fileName) {
                return new InMemoryFileItem(fieldName, contentType, isFormField, fileName);
            }
        };
    }

    public static class InMemoryFileItem implements FileItem {

        private ByteArrayOutputStream baos = new ByteArrayOutputStream();
        private boolean formField;
        private String fieldName;
        private String contentType;
        private String fileName;
        private FileItemHeaders headers;

        public InMemoryFileItem(String fieldName, String contentType, boolean isFormField, String fileName) {
            this.fieldName = fieldName;
            this.contentType = contentType;
            this.formField = isFormField;
            this.fileName = fileName;
            this.headers = new FileItemHeadersImpl();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(baos.toByteArray());
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public String getName() {
            return fileName;
        }

        @Override
        public boolean isInMemory() {
            return true;
        }

        @Override
        public long getSize() {
            return baos.size();
        }

        @Override
        public byte[] get() {
            return baos.toByteArray();
        }

        @Override
        public String getString(String encoding) throws UnsupportedEncodingException {
            return new String(baos.toByteArray(), Charset.forName(encoding));
        }

        @Override
        public String getString() {
            return new String(baos.toByteArray());
        }

        @Override
        public void write(File file) throws Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete() {
            baos.reset();
        }

        @Override
        public String getFieldName() {
            return fieldName;
        }

        @Override
        public void setFieldName(String name) {
            this.fieldName = name;
        }

        @Override
        public boolean isFormField() {
            return formField;
        }

        @Override
        public void setFormField(boolean state) {
            this.formField = state;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return baos;
        }

        @Override
        public FileItemHeaders getHeaders() {
            return headers;
        }

        @Override
        public void setHeaders(FileItemHeaders headers) {
            this.headers = headers;
        }
    }
}
