package org.activityinfo.core.shared;


import org.activityinfo.model.resource.IsResource;
import org.activityinfo.model.resource.ResourceId;

public interface Resource extends IsResource {

    @Override
    ResourceId getId();

}
