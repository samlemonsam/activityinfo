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
package org.activityinfo.store.mysql.update;

import com.google.common.collect.Lists;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.util.List;

public class SqlInsert {
    
    private String tableName;
    private List<String> fields = Lists.newArrayList();
    private List<Object> values = Lists.newArrayList();
    
    private SqlInsert(String tableName) {
        this.tableName = tableName;
    }
    
    public static SqlInsert insertInto(String tableName) {
        return new SqlInsert(tableName);
    }
    
    public SqlInsert value(String fieldName, Number value) {
        return addValue(fieldName, value);
    }
    
    public SqlInsert value(String fieldName, Boolean value) {
        return addValue(fieldName, value);
    }

    public SqlInsert value(String fieldName, String value) {
        if(value != null) {
            value = value.trim();
        }
        return addValue(fieldName, value);
    }
    
    public SqlInsert value(String fieldName, String value, int maxLength) {
        if(value != null) {
            value = value.trim();
            if(value.length() > maxLength) {
                value = value.substring(0, maxLength);
            }
        }
        return addValue(fieldName, value);
    }


    private SqlInsert addValue(String fieldName, Object value) {
        fields.add(fieldName);
        values.add(value);
        return this;
    }
    
    public void execute(QueryExecutor executor) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ");
        sql.append(tableName);
        sql.append("( ");
        for (int i=0;i<fields.size();++i) {
            if(i > 0) {
                sql.append(", ");
            }
            sql.append(fields.get(i));
        }
        sql.append(") VALUES (");
        for (int i=0;i<fields.size();++i) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("?");
        }
        sql.append(")");
        
        executor.update(sql.toString(), values);
    }

}
