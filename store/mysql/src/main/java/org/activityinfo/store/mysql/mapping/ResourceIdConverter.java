package org.activityinfo.store.mysql.mapping;


import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ResourceIdConverter {

    private char domain;
    private int columnIndex;

    public ResourceIdConverter(char domain, int columnIndex) {
        this.domain = domain;
        this.columnIndex = columnIndex;
    }

    public ResourceId toResourceId(ResultSet rs) {
        try {
            int id = rs.getInt(columnIndex);
            assert !rs.wasNull();
            return CuidAdapter.resourceId(domain, id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
