package org.activityinfo.model.type.time;

import org.activityinfo.json.JsonValue;
import org.activityinfo.model.type.FieldTypeClass;

public class FortnightValue implements PeriodValue {

    private final EpiWeek startWeek;

    public FortnightValue(EpiWeek startWeek) {
        this.startWeek = startWeek;
    }

    public FortnightValue(int year, int weekNum) {
        this(new EpiWeek(year, weekNum));
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return FortnightType.TYPE_CLASS;
    }

    @Override
    public JsonValue toJson() {
        return startWeek.toJson();
    }

    public int getYear() {
        return startWeek.getYear();
    }

    @Override
    public LocalDateInterval asInterval() {
        LocalDate startDate = startWeek.asInterval().getStartDate();
        LocalDate endDate = startDate.plusDays(13);

        return new LocalDateInterval(startDate, endDate);
    }

    @Override
    public PeriodValue previous() {
        return new FortnightValue(startWeek.plus(-2));
    }

    @Override
    public PeriodValue next() {
        return new FortnightValue(startWeek.plus(+2));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FortnightValue that = (FortnightValue) o;

        return startWeek.equals(that.startWeek);

    }

    @Override
    public int hashCode() {
        return startWeek.hashCode();
    }

    @Override
    public String toString() {
        return getYear() + toWeekString();
    }

    public String toWeekString() {
        int startWeekNumber = startWeek.getWeekInYear();
        return "W" + startWeekNumber + "-W" + (startWeekNumber+1);
    }

    public int getWeekInYear() {
        return startWeek.getWeekInYear();
    }
}
