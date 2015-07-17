package org.activityinfo.ui.client.page;

import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.form.FormInstance;

public class InstanceUri {

    public static SafeUri of(FormInstance instance) {
        return of(instance.getId());
    }

    private static SafeUri of(ResourceId id) {
        return UriUtils.fromTrustedString("#i/" + id.asString());
    }
}
