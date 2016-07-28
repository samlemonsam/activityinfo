package org.activityinfo.core.shared.criteria;

import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Spike for instance criteria. Defines the criteria
 * for the query as FormInstances that are classes of the
 * given Iri.
 */
public class ClassCriteria implements Criteria {

    private ResourceId classId;


    public ClassCriteria(ResourceId resourceId) {
        this.classId = resourceId;
    }


    @Override
    public boolean apply(@Nonnull FormInstance input) {
        return classId.equals(input.getClassId());
    }

    @Override
    public Criteria copy() {
        return new ClassCriteria(classId);
    }

    public static Criteria union(Set<ResourceId> range) {
        throw new UnsupportedOperationException();
    }

    public ResourceId getClassId() {
        return classId;
    }
}
