package org.activityinfo.store.mysql.cursor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;


class SqlBuilder {

    private String baseTable;
    private Map<String, Integer> columnMap = Maps.newHashMap();
    private List<String> columns = Lists.newArrayList();

    public SqlBuilder(String baseTable) {
        this.baseTable = baseTable;
    }

    public int select(String columnExpr) {
        Integer columnIndex = columnMap.get(columnExpr);
        if(columnIndex == null) {
            columns.add(columnExpr);
            columnIndex = columns.size();
            columnMap.put(columnExpr, columnIndex);
        }
        return columnIndex;
    }

    public String buildSQL() {

        Preconditions.checkState(!columns.isEmpty(), baseTable + ": You must query for one or more columns.");

        String newLine = "\n";
        String indent = "   ";

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
        sql.append("FROM ").append(baseTable).append(newLine);
        return sql.toString();
    }
}
