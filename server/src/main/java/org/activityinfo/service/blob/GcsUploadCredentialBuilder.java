package org.activityinfo.service.blob;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.BaseEncoding;
import org.joda.time.Period;

import java.util.Map;

/**
 * @see <a href="https://developers.google.com/storage/docs/reference-methods?csw=1#postobject">GCS Docs</a>
 */
public class GcsUploadCredentialBuilder {
    private static final String STATUS_CODE = "201";
    private static final String END_POINT_URL_FORMAT = "https://%s.storage.googleapis.com";

    public static final int BYTES_IN_MEGA_BYTE = 1024 * 1024;

    private final GcsPolicyBuilder policyDocument;
    private final Map<String, String> formFields;
    private final AppIdentityService identityService;


    public GcsUploadCredentialBuilder() {
        this(AppIdentityServiceFactory.getAppIdentityService());
    }

    public GcsUploadCredentialBuilder(AppIdentityService identityService) {
        this.policyDocument = new GcsPolicyBuilder();
        this.formFields = Maps.newHashMap();
        this.identityService = identityService;
        policyDocument.successActionStatusMustBe(STATUS_CODE);
    }

    /**
     *
     * @param bucketName The name of the bucket that you want to upload to.
     */
    public GcsUploadCredentialBuilder setBucket(String bucketName) {
        policyDocument.bucketNameMustEqual(bucketName);
        formFields.put("bucket", bucketName);
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

    public GcsUploadCredentialBuilder expireAfter(Period period) {
        policyDocument.expiresAfter(period.toStandardDuration());
        return this;
    }

    public UploadCredentials build() {

        byte[] policy = policyDocument.toJsonBytes();
        String encodedPolicy = BaseEncoding.base64().encode(policy);

        AppIdentityService.SigningResult signature = identityService.signForApp(encodedPolicy.getBytes(Charsets.UTF_8));

        formFields.put("GoogleAccessId", identityService.getServiceAccountName());
        formFields.put("policy", encodedPolicy);
        formFields.put("signature", BaseEncoding.base64().encode(signature.getSignature()));
        formFields.put("success_action_status", STATUS_CODE);

        return new UploadCredentials(String.format(END_POINT_URL_FORMAT, formFields.get("bucket")), "POST", formFields);
    }
}
