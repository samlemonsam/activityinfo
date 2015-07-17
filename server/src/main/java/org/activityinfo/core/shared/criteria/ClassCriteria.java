package org.activityinfo.core.shared.criteria;

import com.google.common.collect.Lists;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.core.shared.Projection;
import org.activityinfo.model.form.FormInstance;

import javax.annotation.Nonnull;
import java.util.List;
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
    public void accept(CriteriaVisitor visitor) {
        visitor.visitClassCriteria(this);
    }


    @Override
    public boolean apply(FormInstance input) {
        return classId.equals(input.getClassId());
    }

    @Override
    public boolean apply(@Nonnull Projection projection) {
        return classId.equals(projection.getRootClassId());
    }

    public static Criteria union(Set<ResourceId> range) {
        if(range.size() == 1) {
            return new ClassCriteria(range.iterator().next());
        } else {
            List<ClassCriteria> criteriaList = Lists.newArrayList();
            for(ResourceId classResourceId : range) {
                criteriaList.add(new ClassCriteria(classResourceId));
            }
            return new CriteriaUnion(criteriaList);
        }
    }

    public ResourceId getClassId() {
        return classId;
    }
}
