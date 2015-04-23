package org.activityinfo.core.shared.criteria;


import org.activityinfo.core.shared.Projection;
import org.activityinfo.model.form.FormInstance;

import javax.annotation.Nonnull;

/**
 * Superclass of {@code Criteria} that are used to select
 * {@code FormInstance}s
 */
public interface Criteria {

    void accept(CriteriaVisitor visitor);

    boolean apply(@Nonnull FormInstance instance);

    boolean apply(@Nonnull Projection projection);


}
