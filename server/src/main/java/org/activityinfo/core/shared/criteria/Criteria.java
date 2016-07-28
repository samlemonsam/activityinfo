package org.activityinfo.core.shared.criteria;


import org.activityinfo.model.form.FormInstance;

import javax.annotation.Nonnull;

/**
 * Superclass of {@code Criteria} that are used to select
 * {@code FormInstance}s
 */
public interface Criteria {

    boolean apply(@Nonnull FormInstance instance);

    Criteria copy();

}
