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
