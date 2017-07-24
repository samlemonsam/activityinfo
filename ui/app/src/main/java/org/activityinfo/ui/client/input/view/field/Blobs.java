package org.activityinfo.ui.client.input.view.field;


import com.google.common.base.Preconditions;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import org.activityinfo.model.resource.ResourceId;

public class Blobs {

    public static SafeUri getAttachmentUri(ResourceId formId, String blobId) {
        return UriUtils.fromTrustedString(getAttachmentUri(blobId, formId));
    }

    public static String getAttachmentUri(String blobId, ResourceId resourceId) {
        return getAttachmentUri(GWT.getHostPageBaseURL(), blobId, resourceId);
    }

    private static String getAttachmentUri(String appUrl, String blobId, ResourceId resourceId) {
        Preconditions.checkNotNull(appUrl);

        if (appUrl.endsWith("/")) {
            appUrl = appUrl.substring(0, appUrl.length() - 1);
        }
        return appUrl + "/service/appengine?blobId=" + blobId + "&resourceId=" + resourceId.asString();
    }
}
