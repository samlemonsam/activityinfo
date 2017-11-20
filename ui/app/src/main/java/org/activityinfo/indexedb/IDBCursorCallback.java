package org.activityinfo.indexedb;

/**
 * Callback interface that handles events from {@link IDBCursor} events.
 *
 * @param T the cursor's value. Must be a subclass of {@link org.activityinfo.json.JsonValue} or
 *          a type annotated with {@code JsType}
 *
 */
public interface IDBCursorCallback<T> {

    void onNext(IDBCursor<T> cursor);

    void onDone();
}
