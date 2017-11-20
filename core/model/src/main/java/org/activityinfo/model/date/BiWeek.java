package org.activityinfo.model.date;

import org.activityinfo.model.type.time.EpiWeek;

/**
 * Created by yuriyz on 8/15/2016.
 */
public class BiWeek {

    private EpiWeek startWeek;

    public BiWeek(EpiWeek startWeek) {
        this.startWeek = startWeek;
    }

    public BiWeek(int startWeekInYear, int year) {
        this(new EpiWeek(year, startWeekInYear));
    }

    public EpiWeek getStartWeek() {
        return startWeek;
    }

    public EpiWeek getEndWeek() {
        return new EpiWeek(startWeek.next());
    }

    public String toString() {
        return startWeek.toString() + "-" + getEndWeek().getWeekInYear();
    }

    /**
     * @param biweek string representation ( e.g. 2016W1-2)
     * @return biweek object
     */
    public static BiWeek parse(String biweek) {
        String[] parts = biweek.split("W");
        String[] subParts = parts[1].split("-");
        return new BiWeek(Integer.parseInt(subParts[0]), Integer.parseInt(parts[0]));
    }

    public BiWeek plus(int count) {
        return new BiWeek(startWeek.plus(2 * count));
    }

    public BiWeek next() {
        return plus(+1);
    }

    public BiWeek previous() {
        return plus(-1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BiWeek biWeek = (BiWeek) o;

        return !(startWeek != null ? !startWeek.equals(biWeek.startWeek) : biWeek.startWeek != null);

    }

    @Override
    public int hashCode() {
        return startWeek != null ? startWeek.hashCode() : 0;
    }
}
