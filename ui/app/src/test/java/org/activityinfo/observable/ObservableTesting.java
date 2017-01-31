package org.activityinfo.observable;

/**
 * Created by alex on 30-1-17.
 */
public class ObservableTesting {

    public static <T> Connection<T> connect(Observable<T> observable) {
        return new Connection<T>(observable);
    }
}
