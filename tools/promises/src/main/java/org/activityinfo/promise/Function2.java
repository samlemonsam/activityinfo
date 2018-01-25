package org.activityinfo.promise;

/**
 * Binary functional interface
 */
public interface Function2<X, Y, R> {

    R apply(X x, Y y);
}
