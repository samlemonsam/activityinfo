package org.activityinfo.store.mysql.cursor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.activityinfo.store.mysql.Join;

import java.util.List;
import java.util.Map;
import java.util.Set;


public class SqlBuilder {

    private String baseFromClause;
    private Map<String, Integer> columnMap = Maps.newHashMap();
    private List<String> columns = Lists.newArrayList();
    private Set<String> joins = Sets.newHashSet();
    private StringBuilder joinClauses = new StringBuilder();
    private String whereClause;
    private String orderByClause;
    private final String newLine;
    private final String indent;

    public SqlBuilder(String baseFromClause) {
        this.baseFromClause = baseFromClause;
        newLine = "\n";
        indent = "   ";
    }
    
    public void where(String whereClause) {
        this.whereClause = whereClause;
    }
    
    public void orderBy(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    private void join(Join join) {
        if(join.getParent() != null) {
            join(join.getParent());
        }
        if(!joins.contains(join.getKey())) {
            joins.add(join.getKey());
            joinClauses.append(newLine).append(indent).append(join.getJoinExpr());
        }
    }


    public int select(Join join, String fieldName) {
        String table;
        if(join == null) {
            table = "base";
        } else {
            join(join);
            table = join.getKey();
        }
        
        String columnExpr = table + "." + fieldName;
        
        Integer columnIndex = columnMap.get(columnExpr);
        if(columnIndex == null) {
            columns.add(columnExpr);
            columnIndex = columns.size();
            columnMap.put(columnExpr, columnIndex);
        }
        return columnIndex;
    }


    public int select(String column) {
        return select(null, column);
    }

    public String buildSQL() {

        Preconditions.checkState(!columns.isEmpty(), baseFromClause + ": You must query for one or more columns.");

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT").append(newLine);
        for(int i=0;i!=columns.size();++i) {
            boolean last = (i == columns.size()-1);
            sql.append(indent).append(columns.get(i));
            if(!last) {
                sql.append(",");
            }
            sql.append(newLine);
        }
        sql.append("FROM ").append(baseFromClause)
                .append(joinClauses)
                .append(newLine);
        
        if(whereClause != null) {
            sql.append("WHERE " ).append(whereClause);
        }
        
        if(orderByClause != null) {
            sql.append(newLine).append("ORDER BY " ).append(orderByClause);
        }

        return sql.toString();
    }
}
