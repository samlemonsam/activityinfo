/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
        assertFired(1);
    }

    public void assertFired(int expectedCount) {
        if(changeCount == 0) {
            throw new AssertionError("onChange() has not been called.");
        }
        if(changeCount != expectedCount) {
            throw new AssertionError("onChange() was called " + changeCount + " times, expected " + expectedCount);
        }
        changeCount = 0;
    }

    public void assertChangeNotFired() {
        if(changeCount > 0) {
            throw new AssertionError("No call to onChange() expected; there have been " + changeCount + " call(s).");
        }
    }
}
