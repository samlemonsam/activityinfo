package org.activityinfo.legacy.shared.command.result;

import org.activityinfo.legacy.shared.model.IndicatorDTO;

import java.util.List;

public class IndicatorResult implements CommandResult {
    private List<IndicatorDTO> indicators;

    public IndicatorResult() {

    }

    public IndicatorResult(List<IndicatorDTO> indicators) {
        this.indicators = indicators;
    }

    public List<IndicatorDTO> getIndicators() {
        return indicators;
    }

    public void setIndicators(List<IndicatorDTO> indicators) {
        this.indicators = indicators;
    }
}
