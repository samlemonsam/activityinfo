package org.activityinfo.model.type.time;

import org.activityinfo.model.resource.IsRecord;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;

import javax.annotation.Nonnull;

/**
 * {@code FieldValue} of type {@link org.activityinfo.model.type.time.LocalDateIntervalType} describing
 * a continuous interval between two {@link org.activityinfo.model.type.time.LocalDate}s,
 * starting on {@code startDate}, inclusive, and ending on {@code endDate}, inclusive.
 */
public class LocalDateInterval implements FieldValue, IsRecord {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public LocalDateInterval(@Nonnull LocalDate startDate, @Nonnull LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     *
     * @return the start date, inclusive of this interval
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * @return the end date, inclusive, of this interval
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return LocalDateIntervalType.TYPE_CLASS;
    }

    @Override
    public Record asRecord() {
        return new Record()
            .set(TYPE_CLASS_FIELD_NAME, getTypeClass().getId())
            .set("start", getStartDate().toString())
            .set("end", getEndDate().toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocalDateInterval that = (LocalDateInterval) o;

        if (endDate != null ? !endDate.equals(that.endDate) : that.endDate != null) return false;
        if (startDate != null ? !startDate.equals(that.startDate) : that.startDate != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = startDate != null ? startDate.hashCode() : 0;
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        return result;
    }
}
