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
package org.activityinfo.server.command.handler.sync;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;


public final class SqliteTypes {

    private static final String TEXT = "TEXT";
    private static final String INT = "INT";
    private static final String REAL = "REAL";

    private SqliteTypes() {}

    private static final Map<Class, String> MAP = new HashMap<>();

    static {
        MAP.put(String.class, TEXT);
        MAP.put(Character.class, TEXT);
        MAP.put(Character.TYPE, TEXT);
        
        MAP.put(Integer.TYPE, INT);
        MAP.put(Integer.class, INT);
        MAP.put(Short.class, INT);
        MAP.put(Short.TYPE, INT);
        MAP.put(Long.class, INT);
        MAP.put(Long.TYPE, INT);
        MAP.put(Byte.class, INT);
        MAP.put(Byte.TYPE, INT);
        MAP.put(Boolean.class, INT);
        MAP.put(Boolean.TYPE, INT);
        
        MAP.put(Float.class, REAL);
        MAP.put(Float.TYPE, REAL);
        MAP.put(Double.class, REAL);
        MAP.put(Double.TYPE, REAL);
    }
    
    public static String getSqliteType(Class javaClass) {
        Preconditions.checkArgument(MAP.containsKey(javaClass), 
                "No sqlite type mapping for java class %s", javaClass.getName());
        
        return MAP.get(javaClass);
    }
}


