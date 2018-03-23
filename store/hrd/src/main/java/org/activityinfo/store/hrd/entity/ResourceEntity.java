package org.activityinfo.store.hrd.entity;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import org.activityinfo.model.database.Resource;
import org.activityinfo.model.database.ResourceType;
import org.activityinfo.model.resource.ResourceId;

/**
 * This is mostly an index entry that allows us
 * to quickly find all resources -- forms, folders, etc -- in a database.
 */
@Entity(name = "Resource")
public class ResourceEntity {

    @Id
    private String id;

    @Index
    private String databaseId;

    @Index
    private String parentId;

    @Index
    private String label;

    @Index
    private ResourceType resourceType;

    public ResourceEntity() {
    }

    public ResourceEntity(ResourceId databaseId, Resource resource) {
        this.id = resource.getId().asString();
        this.databaseId = databaseId.asString();
        this.parentId = resource.getParentId().asString();
        this.label = resource.getLabel();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setId(ResourceId id) {
        this.id = id.asString();
    }

    public String getDatabaseId() {
        return databaseId;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setDatabaseId(String databaseId) {
        this.databaseId = databaseId;
    }

    public void setDatabaseId(ResourceId databaseId) {
        this.databaseId = databaseId.asString();
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setParentId(ResourceId parentId) {
        this.parentId = parentId.asString();
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

}
