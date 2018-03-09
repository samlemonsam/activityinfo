package org.activityinfo.store.mysql.cursor;

import org.activityinfo.model.formula.eval.FieldReader;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.mysql.mapping.FieldValueConverter;

import java.sql.ResultSet;
import java.sql.SQLException;


class ResultSetFieldReader implements FieldReader<ResultSet> {

    private int columnIndex;
    private FieldValueConverter extractor;
    private FieldType type;


    public ResultSetFieldReader(int columnIndex, FieldValueConverter extractor, FieldType type) {
        this.columnIndex = columnIndex;
        this.extractor = extractor;
        this.type = type;
    }

    @Override
    public FieldValue readField(ResultSet rs) {
        try {
            return extractor.toFieldValue(rs, columnIndex);
        } catch (SQLException e) {
            throw new RuntimeException("Exception reading column " + columnIndex + " using " + extractor, e);
        }
    }

    @Override
    public FieldType getType() {
        return type;
    }
}
