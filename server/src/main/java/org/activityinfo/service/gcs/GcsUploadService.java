package org.activityinfo.service.gcs;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.api.client.googleapis.MethodOverride;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.*;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.UploadOptions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.activityinfo.service.DeploymentConfiguration;

import java.io.IOException;
import java.util.List;

/**
 * @author yuriyz on 8/11/14.
 */
@Singleton
public class GcsUploadService {

    public static final String HOST = "https://www.googleapis.com";

    public static final String GOOGLE_STORAGE_PREFIX = "/gs/";
    public static final List<String> OAUTH_SCOPES =
            ImmutableList.of("https://www.googleapis.com/auth/devstorage.read_write");

    private final AppIdentityService appIdentityService = AppIdentityServiceFactory.getAppIdentityService();
    private final BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
//    private final GcsService gcsService = GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());

    private final DeploymentConfiguration config;
    private final HttpTransport transport = new ApacheHttpTransport();
    private final HttpRequestFactory httpRequestFactory = transport.createRequestFactory();

    @Inject
    public GcsUploadService(DeploymentConfiguration config) {
        this.config = config;
    }

    public String createUploadUrl(String formClassId,String fieldId, String blobId) throws IOException {
        return createUploadUrlViaRestApi(formClassId, fieldId, blobId) ;
    }

    private String createUploadUrlViaRestApi(String formClassId, String fieldId, String blobId) throws IOException {
        String bucketName = config.getBlobServiceBucketName();
        GenericUrl initiationRequestUrl = new GenericUrl(HOST + "/upload/storage/v1/b/" + bucketName + "/o");
        String mimeType = "image/png"; // todo pass from client

        HttpResponse initialResponse = executeUploadInitiation(initiationRequestUrl, mimeType);
        return initialResponse.getHeaders().getLocation();
    }

    /**
     * This method sends a POST request with empty content to get the unique upload URL.
     *
     * @param initiationRequestUrl The request URL where the initiation request will be sent
     */
    private HttpResponse executeUploadInitiation(GenericUrl initiationRequestUrl, final String mediaType) throws IOException {
        initiationRequestUrl.put("uploadType", "resumable");

        String accessToken = appIdentityService.getAccessToken(OAUTH_SCOPES).getAccessToken();
        HttpContent content = new EmptyContent() {
            @Override
            public String getType() {
                return mediaType;
            }
        };

        HttpHeaders initiationHeaders = new HttpHeaders();
        HttpRequest request = httpRequestFactory.buildRequest("POST", initiationRequestUrl, content);
        initiationHeaders.set(MediaHttpUploader.CONTENT_TYPE_HEADER, mediaType);
        initiationHeaders.set("Authorization", "Bearer " + accessToken);

        request.getHeaders().putAll(initiationHeaders);
        HttpResponse response = executeCurrentRequest(request);

        return response;
    }

    private HttpResponse executeCurrentRequest(HttpRequest request) throws IOException {
        // enable GZip encoding if necessary
        // method override for non-POST verbs
        new MethodOverride().intercept(request);
        // don't throw an exception so we can let a custom Google exception be thrown
        request.setThrowExceptionOnExecuteError(false);
        // execute the request
        HttpResponse response = request.execute();
        return response;
    }

    private String createUploadUrlViaBlobstoreApi() {
            UploadOptions uploadOptions = UploadOptions.Builder.
                withGoogleStorageBucketName(config.getBlobServiceBucketName()); // force upload to GCS
        return blobstoreService.createUploadUrl("/", uploadOptions); // no success handler
    }
}
