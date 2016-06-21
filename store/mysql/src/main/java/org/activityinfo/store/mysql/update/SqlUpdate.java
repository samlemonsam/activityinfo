package org.activityinfo.store.mysql.update;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.util.List;
import java.util.Objects;


public class SqlUpdate {
    
    private String tableName;
    private List<Object> parameters = Lists.newArrayList();
    
    private String idField;
    private Object idValue;
    
    private StringBuilder setClause = new StringBuilder();
    
    
    public static SqlUpdate update(String tableName) {
        SqlUpdate update = new SqlUpdate();
        update.tableName = tableName;
        return update;
    }
    
    public SqlUpdate where(String fieldName, Object fieldValue) {
        idField = fieldName;
        idValue = fieldValue;
        return this;
    }
    
    public SqlUpdate set(String fieldName, Object value) {
        if(setClause.length() > 0) {
            setClause.append(", ");
        }
        setClause.append(fieldName).append(" = ?");
        parameters.add(value);
        return this;
    }
    
    public SqlUpdate setIfChanged(String fieldName, Object currentValue, Object newValue) {
        if(!Objects.equals(currentValue, newValue)) {
            set(fieldName, newValue);
        }
        return this;
    }

    public void setIfChanged(String fieldName, String currentValue, String newValue, int maxLength) {
        setIfChanged(fieldName, truncate(currentValue, maxLength), truncate(newValue, maxLength));
    }


    public void set(String fieldName, String value, int maxLength) {
        setIfChanged(fieldName, null, value, maxLength);
    }
    
    public static String truncate(String s, int maxLength) {
        if (s == null) {
            return null;
        }
        Preconditions.checkState(maxLength > 0);
        return s.substring(0, Math.min(s.length(), maxLength));
    }
    
    public void execute(QueryExecutor executor) {
        
        if(setClause.length() == 0) {
            return;
        }
        
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ");
        sql.append(tableName);
        sql.append(" SET ");
        sql.append(setClause);
        sql.append(" WHERE ");
        if(idField == null || idValue == null) {
            throw new IllegalStateException("No where clause specified");
        }
        sql.append(idField).append(" = ?");
        
        parameters.add(idValue);
        
        System.out.println("EXECUTING: " + sql.toString());
        
        executor.update(sql.toString(), parameters);
    }

}
