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
