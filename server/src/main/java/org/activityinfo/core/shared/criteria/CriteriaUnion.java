package org.activityinfo.core.shared.criteria;


import com.google.common.collect.Lists;
import org.activityinfo.core.shared.Projection;
import org.activityinfo.model.form.FormInstance;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

public class CriteriaUnion implements Criteria {

    private final List<Criteria> elements;

    public CriteriaUnion(Iterable<? extends Criteria> elements) {
        this.elements = Lists.newArrayList(elements);
    }

    @Override
    public void accept(CriteriaVisitor visitor) {
        visitor.visitUnion(this);
    }

    @Override
    public boolean apply(@Nonnull FormInstance instance) {
        for(Criteria criterium : elements) {
            if(criterium.apply(instance)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean apply(@Nonnull Projection projection) {
        for(Criteria criterium : elements) {
            if(criterium.apply(projection)) {
                return true;
            }
        }
        return false;
    }

    public Collection<Criteria> getElements() {
        return elements;
    }
}
