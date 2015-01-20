package org.activityinfo.store.mysql.mapping;

import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ReferenceValue;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ForeignKeyExtractor implements FieldValueExtractor {
    private char domain;

    public ForeignKeyExtractor(char domain) {
        this.domain = domain;
    }

    @Override
    public FieldValue extract(ResultSet rs, int index) throws SQLException {
        int id = rs.getInt(index);
        if (rs.wasNull()) {
            return null;
        } else {
            return new ReferenceValue(CuidAdapter.cuid(domain, id));
        }
    }
}
