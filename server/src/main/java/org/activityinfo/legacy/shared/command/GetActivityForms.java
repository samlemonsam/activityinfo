package org.activityinfo.legacy.shared.command;

import org.activityinfo.legacy.shared.command.result.ActivityFormResults;
import org.activityinfo.legacy.shared.command.result.ListResult;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;

import java.util.HashSet;
import java.util.Set;

/**
 * Fetches a list of forms based on the selected indicators
 * 
 */
public class GetActivityForms implements Command<ActivityFormResults> {
    
    private Filter filter;

    public GetActivityForms() {
    }

    public GetActivityForms(Filter filter) {
        this.filter = filter;
    }

    public GetActivityForms(Set<Integer> indicatorIds) {
        filter = new Filter();
        filter.addRestriction(DimensionType.Indicator, indicatorIds);
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }
}
