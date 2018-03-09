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

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.activityinfo.model.resource.ResourceId;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.Duration;

import javax.annotation.Nonnull;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * @see <a href="https://developers.google.com/storage/docs/reference-methods?csw=1#postobject">GCS Docs</a>
 */
public class GcsUploadCredentialBuilder {

    private static final String STATUS_CODE = "201";
    private static final String END_POINT_URL_FORMAT = "https://%s.storage.googleapis.com";

    public static final int BYTES_IN_MEGA_BYTE = 1024 * 1024;

    public static final String X_OWNER = "owner";
    public static final String X_CREATOR = "creator";

    public static final String X_GOOG_META_PREFIX = "x-goog-meta-";
    private static final String X_GOOG_META_OWNER = X_GOOG_META_PREFIX + X_OWNER;
    private static final String X_GOOG_META_CREATOR = X_GOOG_META_PREFIX + X_CREATOR;

    private final GcsPolicyBuilder policyDocument;
    private final Map<String, String> formFields;
    private final AppIdentityService identityService;
    private final String contentDispositionValue;
    private String bucketName;

    public GcsUploadCredentialBuilder(@Nonnull AppIdentityService identityService,
                                      @Nonnull String fileName) {

        Preconditions.checkNotNull(identityService);
        Preconditions.checkNotNull(fileName);

        this.formFields = Maps.newHashMap();
        this.identityService = identityService;

        this.contentDispositionValue = "attachment; filename=\"" + fileName + "\"";
        this.policyDocument = new GcsPolicyBuilder();
        this.policyDocument.successActionStatusMustBe(STATUS_CODE);
        this.policyDocument.contentDisposition(contentDispositionValue);
    }

    public GcsUploadCredentialBuilder setOwnerId(ResourceId ownerId) {
        return setXGoogMeta(X_GOOG_META_OWNER, ownerId.asString());
    }

    public GcsUploadCredentialBuilder setCreatorId(ResourceId creatorId) {
        return setXGoogMeta(X_GOOG_META_CREATOR, creatorId.asString());
    }

    public GcsUploadCredentialBuilder setXGoogMeta(String key, String value) {
        this.policyDocument.xGoogMeta(key, value);
        this.formFields.put(key, value);
        return this;
    }

    /**
     * @param bucketName The name of the bucket that you want to upload to.
     */
    public GcsUploadCredentialBuilder setBucket(String bucketName) {
        // Ignore it for now. Bucket hidden field has to be skipped if it is already included into url to avoid
        // "bucket can not be created" error.
        //policyDocument.bucketNameMustEqual(bucketName);
        //formFields.put("bucket", bucketName);
        this.bucketName = bucketName;
        return this;
    }

    public GcsUploadCredentialBuilder setKey(String objectKey) {
        policyDocument.keyMustEqual(objectKey);
        formFields.put("key", objectKey);
        return this;
    }

    public GcsUploadCredentialBuilder setMaxContentLength(long maxBytes) {
        policyDocument.contentLengthMustBeBetween(0, maxBytes);
        return this;
    }

    public GcsUploadCredentialBuilder setMaxContentLengthInMegabytes(int megabytes) {
        return setMaxContentLength(megabytes * BYTES_IN_MEGA_BYTE);
    }

    public GcsUploadCredentialBuilder expireAfter(Duration duration) {
        policyDocument.expiresAfter(duration);
        return this;
    }

    public UploadCredentials build() {
        try {

            Preconditions.checkState(formFields.containsKey(X_GOOG_META_CREATOR), "Creator is not set.");
            Preconditions.checkState(formFields.containsKey(X_GOOG_META_OWNER), "Owner is not set.");

            byte[] policy = policyDocument.toJsonBytes();
            String encodedPolicy = new String(Base64.encodeBase64(policy, false), "UTF-8");

            AppIdentityService.SigningResult signature = identityService.signForApp(encodedPolicy.getBytes(Charsets.UTF_8));

            formFields.put("GoogleAccessId", identityService.getServiceAccountName());
            formFields.put("policy", encodedPolicy);
            formFields.put("signature", new String(Base64.encodeBase64(signature.getSignature(), false), "UTF-8"));
            formFields.put("success_action_status", STATUS_CODE);
            formFields.put("content-disposition", contentDispositionValue);

            String url = String.format(END_POINT_URL_FORMAT, bucketName);
            return new UploadCredentials(url, "POST", formFields);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
