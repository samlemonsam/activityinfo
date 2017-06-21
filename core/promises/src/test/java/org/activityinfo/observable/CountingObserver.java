package org.activityinfo.observable;

public class CountingObserver<T> implements Observer<T> {

    private int changeCount = 0;

    @Override
    public void onChange(Observable<T> observable) {
        System.out.println("Changed.");
        changeCount++;
    }

    /**
     * @return the number of change notifications since the last call to {@code countChanges()}
     */
    public int countChanges() {
        int count = changeCount;
        changeCount = 0;
        return count;
    }
}
