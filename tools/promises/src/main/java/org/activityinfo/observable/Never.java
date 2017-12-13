package org.activityinfo.observable;

/**
 * An observable that never loads.
 */
class Never<T> extends Observable<T> {

    static final Never INSTANCE = new Never();

    @Override
    public boolean isLoading() {
        return true;
    }

    @Override
    public T get() {
        throw new IllegalStateException("loading");
    }
}
