package org.activityinfo.model.query;

@FunctionalInterface
public interface ByteOrder {

    boolean isLessThan(byte a, byte b);
}
