package org.activityinfo.store.mysql.cursor;

import org.activityinfo.model.expr.eval.FieldReader;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.mysql.mapping.FieldValueExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;


class ResultSetFieldReader implements FieldReader<ResultSet> {

    private int columnIndex;
    private FieldValueExtractor extractor;
    private FieldType type;


    public ResultSetFieldReader(int columnIndex, FieldValueExtractor extractor, FieldType type) {
        this.columnIndex = columnIndex;
        this.extractor = extractor;
        this.type = type;
    }

    @Override
    public FieldValue readField(ResultSet rs) {
        try {
            return extractor.extract(rs, columnIndex);
        } catch (SQLException e) {
            throw new RuntimeException("Exception reading column " + columnIndex + " using " + extractor, e);
        }
    }

    @Override
    public FieldType getType() {
        return type;
    }
}
