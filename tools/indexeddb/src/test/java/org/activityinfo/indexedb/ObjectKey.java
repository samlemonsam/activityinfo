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
package org.activityinfo.indexedb;

import com.google.common.annotations.VisibleForTesting;

import java.util.Arrays;

/**
 * 
 */
public class ObjectKey implements Comparable<ObjectKey> {

    private enum KeyType {
        NUMBER,
        DATE,
        STRING,
        BINARY,
        ARRAY;

        public static KeyType of(Object key) {
            if(key instanceof String) {
                return STRING;
            } else if(isArray(key)) {
                return ARRAY;
            } else if(key instanceof Number) {
                return NUMBER;
            } else {
                throw new UnsupportedOperationException("TODO: " + key.getClass());
            }
        }
    }

    private static boolean isArray(Object key) {
        return key.getClass().isArray();
    }


    private Object key;

    public ObjectKey(Object key) {
        this.key = key;
    }

    public String toKeyString() {
        return ((String) key);
    }

    public String[] toKeyArray() {
        return (String[]) key;
    }


    public int toKeyNumber() {
        return ((Number) key).intValue();
    }

    @Override
    public int compareTo(ObjectKey o) {
        return compareKeys(this.key, o.key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if(!(o instanceof ObjectKey)) {
            return false;
        }
        return compareTo(((ObjectKey) o)) == 0;
    }

    @Override
    public int hashCode() {
        if(isArray(key)) {
            return Arrays.hashCode(((Object[]) key));
        } else {
            return key.hashCode();
        }
    }

    @VisibleForTesting
    public static int compareKeys(Object va, Object vb) {
        KeyType ta = KeyType.of(va);
        KeyType tb = KeyType.of(vb);

        if(ta != tb) {
            return ta.ordinal() - tb.ordinal();
        }

        switch (ta) {
            case NUMBER:
                return compareNumberKeys((Number) va, (Number)vb);

            case DATE:
                throw new UnsupportedOperationException("TODO");

            case STRING:
                return compareKeyStrings((String)va, (String)vb);

            case BINARY:
                throw new UnsupportedOperationException("TODO");

            case ARRAY:
                return compareKeyArrays(((Object[]) va), ((Object[])vb));

            default:
                throw new UnsupportedOperationException("type: " + ta);
        }
    }

    private static int compareNumberKeys(Number va, Number vb) {
        return Double.compare(va.doubleValue(), vb.doubleValue());
    }

    @VisibleForTesting
    static int compareKeyStrings(String va, String vb) {
        int length = Math.min(va.length(), vb.length());
        for (int i = 0; i < length; i++) {
            int u = va.codePointAt(i);
            int v = vb.codePointAt(i);
            if(u > v) {
                return 1;
            }
            if(u < v) {
                return -1;
            }
        }
        if(va.length() > vb.length()) {
            return 1;
        }
        if(va.length() < vb.length()) {
            return -1;
        }
        return 0;
    }


    @VisibleForTesting
    static int compareKeyArrays(Object[] va, Object[] vb) {
        int length = Math.min(va.length, vb.length);
        for (int i = 0; i < length; i++) {
            Object u = va[i];
            Object v = vb[i];
            int c = compareKeys(u, v);
            if(c != 0) {
                return c;
            }
        }
        if(va.length > vb.length) {
            return 1;
        }
        if(va.length < vb.length) {
            return -1;
        }

        return 0;
    }
}
