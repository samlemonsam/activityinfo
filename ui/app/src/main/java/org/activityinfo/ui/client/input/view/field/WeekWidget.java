package org.activityinfo.ui.client.input.view.field;

import org.activityinfo.model.type.time.EpiWeek;
import org.activityinfo.model.type.time.EpiWeekType;
import org.activityinfo.ui.client.input.model.FieldInput;

import java.util.ArrayList;
import java.util.List;

public class WeekWidget extends AbstractWeekWidget<EpiWeek> {


    public WeekWidget(FieldUpdater updater) {
        super(EpiWeekType.INSTANCE, updater);
    }

    @Override
    protected List<String> periodList() {
        List<String> weeks = new ArrayList<>();
        for (int i = 1; i <= EpiWeek.WEEKS_IN_YEAR; i++) {
            weeks.add("W" + i);
        }
        return weeks;
    }

    @Override
    protected String yearName(EpiWeek period) {
        return Integer.toString(period.getYear());
    }

    @Override
    protected String periodName(EpiWeek period) {
        return "W" + period.getWeekInYear();
    }

    @Override
    protected FieldInput parseInput(int year, int periodIndex) {
        return new FieldInput(new EpiWeek(year, periodIndex + 1));
    }
}
