package org.activityinfo.model.analysis;

import com.google.common.base.Optional;
import org.immutables.value.Value;

@Value.Immutable
public abstract class SortOrder {

    public abstract Optional<String> getFormula();


    public boolean getAscending() {
        return true;
    }


}
