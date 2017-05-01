package org.activityinfo.observable;

public class ObservableTesting {

    public static <T> Connection<T> connect(Observable<T> observable) {
        return new Connection<T>(observable);
    }
}
