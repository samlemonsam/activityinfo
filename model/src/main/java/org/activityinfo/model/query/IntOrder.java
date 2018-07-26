package org.activityinfo.model.query;

@FunctionalInterface
public interface IntOrder {

    boolean isLessThan(int a, int b);

}
