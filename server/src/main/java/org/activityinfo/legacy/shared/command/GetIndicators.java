package org.activityinfo.legacy.shared.command;

import com.google.common.collect.Sets;
import org.activityinfo.legacy.shared.command.result.IndicatorResult;
import org.activityinfo.legacy.shared.impl.CommandHandlerAsync;

import java.util.HashSet;

public class GetIndicators implements Command<IndicatorResult> {

    private HashSet<Integer> indicatorIds;

    public GetIndicators() {
    }

    public GetIndicators(Iterable<Integer> indicatorIds) {
        this.indicatorIds = Sets.newHashSet(indicatorIds);
    }

    public HashSet<Integer> getIndicatorIds() {
        return indicatorIds;
    }

    public void setIndicatorIds(HashSet<Integer> indicatorIds) {
        this.indicatorIds = indicatorIds;
    }
}
