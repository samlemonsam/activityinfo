package org.activityinfo.server.endpoint.odk.xform;

import com.google.common.base.Optional;
import org.activityinfo.model.resource.ResourceId;
import org.w3c.dom.Element;

/**
 * Provides methods to access the contents of a submitted XFormInstance
 */
public interface XFormInstance {
    String getAuthenticationToken();

    String getId();

    Optional<Element> getFieldContent(ResourceId fieldId);

    ResourceId getFormClassId();
}
