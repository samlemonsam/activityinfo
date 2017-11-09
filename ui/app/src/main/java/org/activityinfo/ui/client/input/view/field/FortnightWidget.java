package org.activityinfo.ui.client.input.view.field;

import org.activityinfo.model.type.time.EpiWeek;
import org.activityinfo.model.type.time.FortnightType;
import org.activityinfo.model.type.time.FortnightValue;
import org.activityinfo.ui.client.input.model.FieldInput;

import java.util.ArrayList;
import java.util.List;

public class FortnightWidget extends AbstractWeekWidget<FortnightValue> {


    public FortnightWidget(FieldUpdater updater) {
        super(FortnightType.INSTANCE, updater);
    }

    @Override
    protected List<String> periodList() {
        List<String> weeks = new ArrayList<>();
        for (int i = 1; i <= EpiWeek.WEEKS_IN_YEAR; i+=2) {
            weeks.add("W" + i + "-" + (i+1));
        }
        return weeks;
    }

    @Override
    protected String yearName(FortnightValue period) {
        return Integer.toString(period.getYear());
    }

    @Override
    protected String periodName(FortnightValue period) {
        return period.toWeekString();
    }


    @Override
    protected FieldInput parseInput(int year, int periodIndex) {
        return new FieldInput(new EpiWeek(year, (periodIndex * 2) + 1));
    }
}
