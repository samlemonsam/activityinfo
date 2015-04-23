package org.activityinfo.core.shared.criteria;

import com.google.common.collect.Lists;
import org.activityinfo.core.shared.Projection;
import org.activityinfo.model.form.FormInstance;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A {@code Criteria} that is satisfied only if all of its
 * children are satisfied
 */
public class CriteriaIntersection implements Criteria, Iterable<Criteria> {

    private final List<Criteria> members;

    public CriteriaIntersection(List<Criteria> members) {
        this.members = members;
    }

    public CriteriaIntersection(Criteria... members) {
        this.members = Lists.newArrayList(members);
    }

    @Override
    public boolean apply(@Nonnull FormInstance input) {
        for(Criteria criteria : members) {
            if(!criteria.apply(input)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean apply(@Nonnull Projection input) {
        for(Criteria criteria : members) {
            if(!criteria.apply(input)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void accept(CriteriaVisitor visitor) {
        visitor.visitIntersection(this);
    }

    @Override
    public Iterator<Criteria> iterator() {
        return members.iterator();
    }

    public Collection<Criteria> getElements() {
        return members;
    }
}
