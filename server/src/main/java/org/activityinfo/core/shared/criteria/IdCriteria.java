package org.activityinfo.core.shared.criteria;

import com.google.common.collect.Sets;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Matches Instances that belong to the given set
 */
public class IdCriteria implements Criteria {

    private final Set<ResourceId> instanceIds;
    private boolean mappedToLegacyModel = true;
    private ResourceId formId;

    public IdCriteria(ResourceId formId, ResourceId instanceIds) {
        this.formId = formId;
        this.instanceIds = Sets.newHashSet(instanceIds);
    }

    public IdCriteria(ResourceId formId, Set<ResourceId> instanceIds) {
        this.formId = formId;
        this.instanceIds = instanceIds;
    }

    public IdCriteria(ResourceId formId, Iterable<ResourceId> instanceIds) {
        this.formId = formId;
        this.instanceIds = Sets.newHashSet(instanceIds);
    }

    public ResourceId getFormId() {
        return formId;
    }

    public Set<ResourceId> getInstanceIds() {
        return instanceIds;
    }

    @Override
    public boolean apply(@Nonnull FormInstance instance) {
        return instanceIds.contains(instance.getId());
    }

    public boolean isMappedToLegacyModel() {
        return mappedToLegacyModel;
    }

    public IdCriteria setMappedToLegacyModel(boolean mappedToLegacyModel) {
        this.mappedToLegacyModel = mappedToLegacyModel;
        return this;
    }

    @Override
    public Criteria copy() {
        return new IdCriteria(formId, instanceIds);
    }
}
