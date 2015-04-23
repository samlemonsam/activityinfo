package org.activityinfo.core.shared.criteria;

import com.google.common.collect.Sets;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.core.shared.Projection;
import org.activityinfo.model.form.FormInstance;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Matches Instances that belong to the given set
 */
public class IdCriteria implements Criteria {

    private final Set<ResourceId> instanceIds;

    public IdCriteria(ResourceId... instanceIds) {
        this.instanceIds = Sets.newHashSet(instanceIds);
    }

    public IdCriteria(Set<ResourceId> instanceIds) {
        this.instanceIds = instanceIds;
    }

    public IdCriteria(Iterable<ResourceId> instanceIds) {
        this.instanceIds = Sets.newHashSet(instanceIds);
    }

    public Set<ResourceId> getInstanceIds() {
        return instanceIds;
    }

    @Override
    public void accept(CriteriaVisitor visitor) {
        visitor.visitInstanceIdCriteria(this);
    }

    @Override
    public boolean apply(@Nonnull FormInstance instance) {
        return instanceIds.contains(instance.getId());
    }

    @Override
    public boolean apply(@Nonnull Projection projection) {
        return instanceIds.contains(projection.getRootInstanceId());
    }
}
