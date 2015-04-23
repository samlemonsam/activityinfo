package org.activityinfo.legacy.shared.adapter;

import com.google.common.base.Function;
import org.activityinfo.core.client.NotFoundException;
import org.activityinfo.model.form.FormInstance;

import java.util.List;

/**
 * Selects a single FormInstance from a list, or throws a NotFoundException if there are none
 */
public class SelectSingle implements Function<List<FormInstance>, FormInstance> {
    @Override
    public FormInstance apply(List<FormInstance> input) {
        if (input.isEmpty()) {
            throw new NotFoundException();
        } else {
            return input.get(0);
        }
    }
}
