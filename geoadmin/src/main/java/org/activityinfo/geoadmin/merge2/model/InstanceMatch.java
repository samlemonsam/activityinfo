package org.activityinfo.geoadmin.merge2.model;

import org.activityinfo.model.resource.ResourceId;

/**
 * Explicit matching between a Resource in the source collection to an existing resource in the target collection.
 * 
 * <p>For example, if we are updating an <em>existing</em> (target) collection of provinces with a new (source) shapefile
 * containing corrections, than each {@code InstanceMatch} in the model would explicitly match the province in
 * the shapefile to the correct province in the existing collection of provinces.</p>
 * 
 * <p>{@code InstanceMatch}es complement or override the matching done automatically; an {@code InstanceMatch} would
 * be necessary to match a province in the source and target sets which have no obvious connection, for example,
 * the Egyptian governorate of al-Wādī l-Ǧadīd is sometimes referred to by its literal English translation, "New Valley".
 * </p>
 * 
 * <p>In this case, updating an existing (target) province called "New Valley" with a new (source) province
 * called "al Wadi Al Gadid" would require the user to add an explicit {@code InstanceMatch} through the UI.</p>
 */
public class InstanceMatch {
    
    private ResourceId sourceId;
    private ResourceId targetId;

    public InstanceMatch(ResourceId sourceId, ResourceId targetId) {
        this.sourceId = sourceId;
        this.targetId = targetId;
    }

    public ResourceId getSourceId() {
        return sourceId;
    }

    public ResourceId getTargetId() {
        return targetId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstanceMatch that = (InstanceMatch) o;

        if (!sourceId.equals(that.sourceId)) return false;
        if (!targetId.equals(that.targetId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = sourceId.hashCode();
        result = 31 * result + targetId.hashCode();
        return result;
    }
}
