package org.activityinfo.ui.client.component.form.subform;


/**
 * A cursor over a sequence of periods. 
 */
public interface PeriodCursor {

    /**
     * Gets the {@code i}-th period, relative to the current position.
     */
    Tab get(int i);

    /**
     * Advances (or retreats) the cursor {@code count} periods relative to the current
     * position.
     */
    void advance(int count);
}
