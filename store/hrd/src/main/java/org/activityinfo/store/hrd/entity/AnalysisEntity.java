package org.activityinfo.store.hrd.entity;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Unindex;

/**
 *
 */
@Entity(name = "Analysis")
public class AnalysisEntity {

    @Id
    private String id;

    /**
     * The id of the parent catalog entry
     */
    @Index
    private String parentId;

    @Index
    private String type;

    /**
     * The analysis model serialized as JSON
     */
    @Unindex
    private String model;

    @Index
    private String label;

    /**
     * The version of the entity
     */
    private long version;



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
