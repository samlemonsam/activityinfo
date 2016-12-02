package org.activityinfo.ui.client.component.form.subform;


/**
 * A cursor over a sequence of periods. 
 */
public interface PeriodCursor<T> {

    /**
     * Gets the {@code i}-th period, relative to the current position.
     */
    Tab get(int i);

    Tab get(T period);

    Tab get(String dataPeriod);

    /**
     * Advances (or retreats) the cursor {@code count} periods relative to the current
     * position.
     */
    void advance(int count);

    T getValue(String dataPeriod);

    T getCurrentPeriod();

    void setCurrentPeriod(T period);
}
