package org.activityinfo.observable;

public class MockObserver<T> implements Observer<T> {
    private int changeCount;
    
    @Override
    public void onChange(Observable<T> observable) {
        changeCount++;
    }
    
    public void resetCount() {
        changeCount = 0;
    }

    public int getChangeCount() {
        return changeCount;
    }

    public void assertChangeFiredOnce() {
        if(changeCount == 0) {
            throw new AssertionError("onChange() has not been called.");
        }
        if(changeCount > 1) {
            throw new AssertionError("onChange() was called " + changeCount + " times");
        }
        changeCount = 0;
    }

    public void assertChangeNotFired() {
        if(changeCount > 0) {
            throw new AssertionError("No call to onChange() expected; there have been " + changeCount + " call(s).");
        }
    }
}
