package org.activityinfo.store.query.impl;

import com.google.common.base.Preconditions;

/**
 * Convenience implementation of Slot that ensures that null pointers
 * are not read or written.
 */
public class PendingSlot<T> implements Slot<T> {

    private T value = null;


    public PendingSlot() {
    }

    public PendingSlot(T value) {
        Preconditions.checkNotNull(value, "Slot value cannot be null");
        this.value = value;
    }

    public void set(T value) {
        Preconditions.checkNotNull(value, "Slot value cannot be null");
        Preconditions.checkState(this.value == null,
                "Slot value has already been set. this.value = " + this.value + ", value = " + value);

        this.value = value;
    }

    @Override
    public T get() {
        Preconditions.checkState(this.value != null, "Slot value has not been set.");
        return this.value;
    }
}
