package org.activityinfo.store.hrd.op;

import com.googlecode.objectify.VoidWork;
import org.activityinfo.model.database.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.Hrd;
import org.activityinfo.store.hrd.entity.ResourceEntity;

public class UpdateResource extends VoidWork {

    private ResourceId databaseId;
    private Resource resource;

    public UpdateResource(ResourceId databaseId, Resource resource) {
        this.databaseId = databaseId;
        this.resource = resource;
    }

    @Override
    public void vrun() {
        Hrd.ofy().save().entity(new ResourceEntity(databaseId, resource)).now();
    }
}
