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
package org.activityinfo.server.endpoint.odk;

import com.google.inject.util.Providers;
import com.sun.jersey.api.core.InjectParam;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.DeploymentConfiguration;
import org.activityinfo.server.blob.GcsBlobFieldStorageService;
import org.activityinfo.store.spi.BlobAuthorizer;
import org.activityinfo.store.spi.BlobId;
import org.activityinfo.store.spi.FormStorageProvider;

import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

public class TestBlobstoreService extends GcsBlobFieldStorageService implements BlobAuthorizer {

    public TestBlobstoreService(DeploymentConfiguration config, final EntityManager em, final FormStorageProvider formStorage) {
        super(config, Providers.of(em), Providers.of(formStorage));
    }

    @Override
    public Response getBlobUrl(@InjectParam AuthenticatedUser user, BlobId blobId, ResourceId resourceId) {
        throw new UnsupportedOperationException();
    }

    public void put(AuthenticatedUser user, String contentDisposition, String mimeType,
                    BlobId blobId, ResourceId formId, InputStream inputStream) throws IOException {

    }

    @Override
    public Response getImage(@InjectParam AuthenticatedUser user, BlobId blobId, ResourceId resourceId) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Response getImageUrl(@InjectParam AuthenticatedUser user, BlobId blobId, ResourceId resourceId) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Response getThumbnail(@InjectParam AuthenticatedUser user, BlobId blobId, ResourceId resourceId, int width, int height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Response exists(@InjectParam AuthenticatedUser user, BlobId blobId, ResourceId resourceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Response getUploadCredentials(@InjectParam AuthenticatedUser user, BlobId blobId, ResourceId resourceId, String fileName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOwner(int userId, String blobId) {
        return false;
    }
}
