package org.activityinfo.core.shared.criteria;

import org.activityinfo.core.shared.Projection;
import org.activityinfo.model.form.FormInstance;

import javax.annotation.Nonnull;

/**
 * Null Criteria. (Includes all instances)
 */
public class NullCriteria implements Criteria {

    public static final NullCriteria INSTANCE = new NullCriteria();

    private NullCriteria() {

    }

    @Override
    public void accept(CriteriaVisitor visitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean apply(@Nonnull FormInstance instance) {
        return true;
    }

    @Override
    public boolean apply(@Nonnull Projection projection) {
        return true;
    }
}
